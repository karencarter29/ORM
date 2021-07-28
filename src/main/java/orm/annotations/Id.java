package orm.annotations;

import java.lang.annotation.*;

@Column
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id {}