package ru.javaSchoolProject.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;
import ru.javaSchoolProject.dto.ContractDto;
import ru.javaSchoolProject.dto.OptionsDto;
import ru.javaSchoolProject.enums.OptionType;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contract")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "phone_number", unique = true)
    private Long phoneNumber;

    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contract")
    private List<ContractOptions> contractOptions; // current options of user

    @OneToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "tariff_id")
    private Tariff tariff;

    public static ContractBuilder builder(){
        return new ContractBuilder();
    }
    @Data
    public static class ContractBuilder {
        private int id;
        private Long phoneNumber;
        private User user;
        private List<ContractOptions> contractOptions; // current options of user
        private Tariff tariff;

        public ContractBuilder setContractOptions(ContractDto contractDto, Contract contract) {
            List<ContractOptions> newContractOptions = new ArrayList<>();

            //make contractOptions from optionDto
            if (contractDto.getContractOptions() != null) {
                for (OptionsDto opDto : contractDto.getContractOptions()) {
                    ContractOptions newOption = new ContractOptions();
                    newOption.setContract(contract);
                    newOption.setOptionId(Integer.parseInt(opDto.getId()));
                    newOption.setName(opDto.getName());
                    newOption.setOptionType(OptionType.valueOf(opDto.getOptionType()));
                    newOption.setCost(Double.parseDouble(opDto.getCost()));

                    newContractOptions.add(newOption);
                }
            }
            this.contractOptions = newContractOptions;
            return this;
        }

        public Contract build(ContractDto contractDto) {
            Contract contract = new Contract();
            this.setContractOptions(contractDto, contract);
            contract.setContractOptions(this.contractOptions);
            return contract;
        }
    }

}
