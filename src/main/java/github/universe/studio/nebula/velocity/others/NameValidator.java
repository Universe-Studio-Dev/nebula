package github.universe.studio.nebula.velocity.others;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file NameValidator
 */
public class NameValidator {

    public boolean isValid(String name) {
        return name.length() >= 3 && name.length() <= 16 && name.matches("[a-zA-Z0-9_]+");
    }
}