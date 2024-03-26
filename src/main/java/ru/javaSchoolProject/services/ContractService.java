package ru.javaSchoolProject.services;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.javaSchoolProject.dao.ContractDao;
import ru.javaSchoolProject.dao.TariffDao;
import ru.javaSchoolProject.dao.UserDao;
import ru.javaSchoolProject.dto.*;
import ru.javaSchoolProject.exceptions.ContractException;
import ru.javaSchoolProject.models.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ContractService {

    @Autowired
    private ContractDao contractDao;

    @Autowired
    private TariffDao tariffDao;

    @Autowired
    private UserDao userDao;

    final static Logger logger = Logger.getLogger(TariffService.class.getName());

    public ContractAnswerDto signContract(ContractDto contractDto) {
        try {
            checkContractDto(contractDto);
            User currentUser = userDao.findById(Integer.parseInt(contractDto.getUserId()));
            if (currentUser.isBlocked()) {
                throw new ContractException("User is blocked");
            }
            Tariff tariff = tariffDao.findTariffById(Integer.parseInt(contractDto.getTariffId()));
            if (tariff == null) {
                throw new ContractException("Tariff not found");
            }
            if (!checkContractDtoOptions(contractDto, tariff)) {
                throw new ContractException("cant save contract options are invalid");
            }

            Contract newContract = Contract.builder().build(contractDto);
            //make contract
            newContract.setPhoneNumber(Long.parseLong(contractDto.getPhoneNumber()));
            newContract.setTariff(tariffDao.findTariffById(Integer.parseInt(contractDto.getTariffId())));
            newContract.setUser(currentUser);

            if (!contractDao.save(newContract)) {
                //NOT unique phone number
                throw new ContractException("Telephone number: " + newContract.getPhoneNumber() + " is already taken");
            }
        } catch (ContractException ex) {
            return new ContractAnswerDto(ex.getMessage());
        }
        return new ContractAnswerDto("Success");
    }

    public FullContractDto getContract(ContractIdAndNumberDto contractIdAndNumberDto) {
        try { //easy check
            Integer.parseInt(contractIdAndNumberDto.getContractId());
            Long.parseLong(contractIdAndNumberDto.getPhoneNumber());
        } catch (NumberFormatException e) {//return null
            return new FullContractDto();
        }

        //dao contract
        Contract currentContract = contractDao.getContractById(Integer.parseInt(contractIdAndNumberDto.getContractId()));
        //contract not found
        if (
                currentContract == null
                || currentContract.getPhoneNumber() != Long.parseLong(contractIdAndNumberDto.getPhoneNumber()) //wrong number
        ) {
            return new FullContractDto();
        }

        Tariff currentTariff = currentContract.getTariff();
        TariffDto tariffDto = TariffDto.builder().setOptions(currentContract).build();
        tariffDto.setId(currentTariff.getId());
        tariffDto.setTitle(currentTariff.getTitle());
        tariffDto.setDescription(currentTariff.getDescription());
        tariffDto.setCost(String.valueOf(currentTariff.getCost()));
        tariffDto.setActive(currentTariff.isActive());

        FullContractDto foundContract = FullContractDto.builder().setOptions(currentContract).build();
        foundContract.setId(String.valueOf(currentContract.getId()));
        foundContract.setPhoneNumber(String.valueOf(currentContract.getPhoneNumber()));
        foundContract.setUserId(String.valueOf(currentContract.getUser().getId()));
        foundContract.setTariffDto(tariffDto);

        return foundContract;

    }

    public List<ContractIdAndNumberDto> getContractIdsAndNumbers(String userId) {
        try {
            Integer.parseInt(userId);
        } catch (NumberFormatException e) {// cant parse userId
            return new ArrayList<>();
        }
        //get contract info by user id
        List<Contract> currentContracts = contractDao.getContractsOfUser(Integer.parseInt(userId));
        //initialize dto response entity
        List<ContractIdAndNumberDto> foundContractIdAndNumberDtoList = new ArrayList<>();

        if (currentContracts != null) {
            for (Contract c : currentContracts) {
                foundContractIdAndNumberDtoList.add(new ContractIdAndNumberDto(String.valueOf(c.getId()), String.valueOf(c.getPhoneNumber())));
            }
        }
        return foundContractIdAndNumberDtoList;
    }

    public ContractAnswerDto deleteContract(String id) {

        try {
            try { //easy check
                Integer.parseInt(id);
            } catch (NumberFormatException e) {
                throw new ContractException("Invalid id");
            }
            Contract currentContract = contractDao.getContractById(Integer.parseInt(id));
            if (currentContract == null) {
                throw new ContractException("Cant delete tariff: tariff not found");
            }
            if (currentContract.getUser().isBlocked()) {
                throw new ContractException("Cant delete tariff: user is blocked");
            }
            if (!contractDao.deleteContract(Integer.parseInt(id))) {
                throw new ContractException("Cant delete contract from database");
            }
        } catch (ContractException ex) {
            return new ContractAnswerDto(ex.getMessage());
        }
        return new ContractAnswerDto("Delete success");
    }

    public List<ContractInfoAboutUserDto> getAllContractsUserInfo() {
        //get all contract info about users
        List<Contract> currentContracts = contractDao.getAllContracts();
        //initialize dto response entity
        List<ContractInfoAboutUserDto> foundContractInfoAboutUserDtoList = new ArrayList<>();

        if (currentContracts != null) {
            for (Contract c : currentContracts) {
                foundContractInfoAboutUserDtoList.add(
                        new ContractInfoAboutUserDto(String.valueOf(c.getId()),
                                String.valueOf(c.getPhoneNumber()),
                                String.valueOf(c.getUser().getId()),
                                c.getUser().getLogin()));
            }
        }
        return foundContractInfoAboutUserDtoList;
    }


    //validators
    private static void checkContractDto(ContractDto contractDto) throws ContractException {
        try {
            Integer.parseInt(contractDto.getTariffId());
            Long.parseLong(contractDto.getPhoneNumber());
            Integer.parseInt(contractDto.getUserId());
            if (
                    contractDto.getPhoneNumber().length() != 11
                            || !contractDto.getPhoneNumber().startsWith("8777")
            ) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            throw new ContractException("Invalid contract data(wrong phone number or user/tariff id)");
        }
    }

    private static boolean checkContractDtoOptions(ContractDto contractDto, Tariff currentTariff) {
        List<OptionsDto> chosenOptions = contractDto.getContractOptions();
        if (chosenOptions == null) {// no chosen options
            return true;
        }
        List<Options> currentOptions = currentTariff.getOptions();

        if (chosenOptions.size() > currentOptions.size()) { // you cant choose more options than possible
            logger.warn("CANT SIGN CONTRACT: options size is more than max");
            return false;
        }

        Set<Integer> optionIds = new HashSet<Integer>();
        for (Options op : currentOptions) { //create set of allowed options for current tariff
            optionIds.add(op.getId());
        }

        for (OptionsDto opDto : chosenOptions) {
            try {
                Integer.parseInt(opDto.getId());
                Double.parseDouble(opDto.getCost());
            } catch (NumberFormatException e) {
                logger.warn("CANT SIGN CONTRACT: wrong type of parameter in option");
                return false;
            }
            if (!optionIds.contains(Integer.parseInt(opDto.getId()))) {
                logger.warn("CANT SIGN CONTRACT: no option with id = " + opDto.getId());
                return false;
            }


        }
        return true;
    }

}
