package ru.javaSchoolProject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.javaSchoolProject.models.Contract;
import ru.javaSchoolProject.models.ContractOptions;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
public class FullContractDto {

    private String id;

    private String userId;

    private TariffDto tariffDto;

    private String phoneNumber;

    private List<OptionsDto> contractOptions;

    public static FullContractDtoBuilder builder() {
        return new FullContractDtoBuilder();
    }

    public static class FullContractDtoBuilder {
        private String id;

        private String userId;

        private TariffDto tariffDto;

        private String phoneNumber;

        private List<OptionsDto> contractOptions;


        public FullContractDtoBuilder setOptions(Contract currentContract) {
            //parse contractOptions to optionDtos
            List<OptionsDto> foundContractOptions = new ArrayList<>();
            if (currentContract.getContractOptions() != null) {
                for (ContractOptions op : currentContract.getContractOptions()) {
                    OptionsDto foundOption = new OptionsDto();
                    foundOption.setId(String.valueOf(op.getId()));
                    foundOption.setName(op.getName());
                    foundOption.setOptionType(String.valueOf(op.getOptionType()));
                    foundOption.setCost(String.valueOf(op.getCost()));

                    foundContractOptions.add(foundOption);
                }
            }

            this.contractOptions = foundContractOptions;

            return this;
        }

        public FullContractDto build() {
            return new FullContractDto(id, userId, tariffDto, phoneNumber, contractOptions);
        }
    }
}