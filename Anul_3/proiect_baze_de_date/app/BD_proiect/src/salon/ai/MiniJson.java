package salon.ai;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Parser JSON minimal, ca proiectul să nu depindă de biblioteci externe. */
final class MiniJson {

    private final String text;
    private int index;

    private MiniJson(String text) {
        this.text = text;
    }

    static Object parse(String text) {
        MiniJson parser = new MiniJson(text);
        parser.skipSpaces();
        Object value = parser.readValue();
        parser.skipSpaces();

        if (parser.index != text.length()) {
            throw new IllegalArgumentException("Răspuns JSON invalid.");
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> map(Object value) {
        if (value instanceof Map<?, ?>) {
            return (Map<String, Object>) value;
        }

        throw new IllegalArgumentException("Se aștepta un obiect JSON.");
    }

    @SuppressWarnings("unchecked")
    static List<Object> list(Object value) {
        if (value instanceof List<?>) {
            return (List<Object>) value;
        }

        throw new IllegalArgumentException("Se aștepta o listă JSON.");
    }

    static String quote(String value) {
        StringBuilder result = new StringBuilder("\"");

        for (int position = 0; position < value.length(); position++) {
            char c = value.charAt(position);

            switch (c) {
                case '"':
                    result.append("\\\"");
                    break;
                case '\\':
                    result.append("\\\\");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        result.append(String.format("\\u%04x", (int) c));
                    } else {
                        result.append(c);
                    }
            }
        }

        return result.append('"').toString();
    }

    private IllegalArgumentException fail() {
        return new IllegalArgumentException("Răspuns JSON invalid.");
    }

    private void skipSpaces() {
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
    }

    private void expect(char expected) {
        if (index >= text.length() || text.charAt(index) != expected) {
            throw fail();
        }

        index++;
    }

    private Object readValue() {
        if (index >= text.length()) {
            throw fail();
        }

        char c = text.charAt(index);

        if (c == '{') {
            return readObject();
        }

        if (c == '[') {
            return readArray();
        }

        if (c == '"') {
            return readString();
        }

        if (text.startsWith("true", index)) {
            index += 4;
            return Boolean.TRUE;
        }

        if (text.startsWith("false", index)) {
            index += 5;
            return Boolean.FALSE;
        }

        if (text.startsWith("null", index)) {
            index += 4;
            return null;
        }

        return readNumber();
    }

    private Map<String, Object> readObject() {
        Map<String, Object> result = new LinkedHashMap<>();

        expect('{');
        skipSpaces();

        if (index < text.length() && text.charAt(index) == '}') {
            index++;
            return result;
        }

        while (true) {
            skipSpaces();
            String key = readString();
            skipSpaces();
            expect(':');
            skipSpaces();
            result.put(key, readValue());
            skipSpaces();

            if (index >= text.length()) {
                throw fail();
            }

            char c = text.charAt(index++);

            if (c == '}') {
                return result;
            }

            if (c != ',') {
                throw fail();
            }
        }
    }

    private List<Object> readArray() {
        List<Object> result = new ArrayList<>();

        expect('[');
        skipSpaces();

        if (index < text.length() && text.charAt(index) == ']') {
            index++;
            return result;
        }

        while (true) {
            skipSpaces();
            result.add(readValue());
            skipSpaces();

            if (index >= text.length()) {
                throw fail();
            }

            char c = text.charAt(index++);

            if (c == ']') {
                return result;
            }

            if (c != ',') {
                throw fail();
            }
        }
    }

    private String readString() {
        expect('"');

        StringBuilder result = new StringBuilder();

        while (index < text.length()) {
            char c = text.charAt(index++);

            if (c == '"') {
                return result.toString();
            }

            if (c != '\\') {
                result.append(c);
                continue;
            }

            if (index >= text.length()) {
                break;
            }

            char escaped = text.charAt(index++);

            switch (escaped) {
                case '"':
                    result.append('"');
                    break;
                case '\\':
                    result.append('\\');
                    break;
                case '/':
                    result.append('/');
                    break;
                case 'b':
                    result.append('\b');
                    break;
                case 'f':
                    result.append('\f');
                    break;
                case 'n':
                    result.append('\n');
                    break;
                case 'r':
                    result.append('\r');
                    break;
                case 't':
                    result.append('\t');
                    break;
                case 'u':
                    if (index + 4 > text.length()) {
                        throw fail();
                    }

                    result.append((char) Integer.parseInt(text.substring(index, index + 4), 16));
                    index += 4;
                    break;
                default:
                    throw fail();
            }
        }

        throw fail();
    }

    private Double readNumber() {
        int start = index;

        while (index < text.length() && "+-0123456789.eE".indexOf(text.charAt(index)) >= 0) {
            index++;
        }

        if (start == index) {
            throw fail();
        }

        return Double.valueOf(text.substring(start, index));
    }
}
