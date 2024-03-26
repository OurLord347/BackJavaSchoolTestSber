package ru.javaSchoolProject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.javaSchoolProject.models.Contract;
import ru.javaSchoolProject.models.Options;
import ru.javaSchoolProject.models.Tariff;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TariffDto {

    private Integer id;

    private String title;

    private String description;

    private String cost;

    private boolean isActive;

    private List<OptionsDto> options;

    public static TariffDtoBuilder builder() {
        return new TariffDtoBuilder();
    }

    public static class TariffDtoBuilder {
        private Integer id;
        private String title;
        private String description;
        private String cost;
        private boolean isActive;
        private List<OptionsDto> options;

        public TariffDtoBuilder setOptions(Contract currentContract) {
            //parse options of tariff in contract to optionsDto (all options of tariff)
            Tariff currentTariff = currentContract.getTariff();
            List<OptionsDto> foundTariffOptions = new ArrayList<>();
            if (currentTariff.getOptions() != null) {
                for (Options op : currentContract.getTariff().getOptions()) {
                    OptionsDto foundOption = new OptionsDto();
                    foundOption.setId(String.valueOf(op.getId()));
                    foundOption.setName(op.getName());
                    foundOption.setOptionType(String.valueOf(op.getOptionType()));
                    foundOption.setCost(String.valueOf(op.getCost()));

                    foundTariffOptions.add(foundOption);
                }
            }
            this.options = foundTariffOptions;
            return this;
        }

        public TariffDto build() {
            return new TariffDto(id, title, description, cost, isActive, options);
        }
    }
}
