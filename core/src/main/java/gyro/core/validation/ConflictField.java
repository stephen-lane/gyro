package gyro.core.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Repeatable(ConflictFields.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ValidatorClass(ConflictFieldValidator.class)
public @interface ConflictField {
    String source();
    String conflict();
}
