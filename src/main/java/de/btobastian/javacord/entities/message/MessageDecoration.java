package de.btobastian.javacord.entities.message;

/**
 * All basic decorations available in discord.
 */
public enum MessageDecoration {

    ITALICS("*"),
    BOLD("**"),
    STRIKEOUT("~~"),
    CODE_SIMPLE("`"),
    CODE_LONG("```"),
    UNDERLINE("__");

    /**
     * The prefix of the decoration.
     */
    private final String prefix;

    /**
     * The suffix of the decoration.
     */
    private final String suffix;

    /**
     * Creates a new message decoration.
     *
     * @param prefix The prefix of the decoration.
     */
    MessageDecoration(String prefix) {
        this.prefix = prefix;
        this.suffix = new StringBuilder(prefix).reverse().toString();
    }

    /**
     * Gets the prefix of the decoration.
     *
     * @return The prefix of the decoration.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets the suffix of the decoration.
     *
     * @return The suffix of the decoration.
     */
    public String getSuffix() {
        return suffix;
    }

}