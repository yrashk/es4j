package org.eventchain.layout;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When used with a getter or a setter, makes {@link Layout} ignore
 * the corresponding property.
 *
 * <h1>Examples</h1>
 *
 * <pre>
 * {@code
 *     @ LayoutIgnore
 *     public String getMyProperty() {
 *         ...
 *     }
 * }
 * </pre>
 *
 * It can be also used with Lombok:
 *
 * <pre>
 * {@code
 *     @ Getter(onMethod=@ __(@ LayoutIgnore)) @ Setter
 *     private String myProperty;
 * }
 * </pre>
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LayoutIgnore {
}
