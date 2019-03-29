package gyro.core.validation;

import com.google.common.primitives.Doubles;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class AllowedNumbersValidator extends AnnotationNumberBaseProcessor<AllowedNumbers> {
    private static AllowedNumbersValidator constructor = new AllowedNumbersValidator();

    private AllowedNumbersValidator() {
    }

    public static AnnotationProcessor getAnnotationProcessor() {
        return constructor;
    }

    @Override
    boolean doValidation(Object value) {
        HashSet<Double> validValues = new HashSet(Doubles.asList(annotation.value()));
        if (value instanceof Number) {
            double valueCheck = ValidationUtils.getDoubleValue(value);
            return validValues.contains(valueCheck);
        } else if (value instanceof List && ((List) value).size() > 0
            && ((List) value).get(0) instanceof Number) {
            return ((List) value).stream().map(ValidationUtils::getDoubleValue).allMatch(validValues::contains);
        } else if (value instanceof Map && ((Map) value).keySet().size() > 0
            && ((Map) value).keySet().toArray()[0] instanceof Number) {
            return ((Map) value).keySet().stream().map(ValidationUtils::getDoubleValue).allMatch(validValues::contains);
        } else {
            return true;
        }
    }

    @Override
    public String getMessage() {
        return String.format(annotation.message(), isDouble
            ? Arrays.toString(annotation.value())
            : Arrays.toString(Arrays.stream(annotation.value()).mapToLong(o -> (long)o).toArray()));
    }
}
