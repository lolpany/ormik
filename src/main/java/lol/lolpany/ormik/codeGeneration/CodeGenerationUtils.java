package lol.lolpany.ormik.codeGeneration;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static lol.lolpany.ormik.codeGeneration.JdbcCallbackGenerationUtils.generateSelect;

/**
 * Place generation code in go method and run it as test.
 * Caveats:
 * 1 for jdbc callbacks generation
 * - bean name must contain table name in upper camel case
 * - bean fields must be named same as db table fields except for 'b_' prefix
 * 2 for convertation methods generation
 * - names of fields in both beans must be same
 * - put convert on separate line (dont put it as method parameter call)
 * - no generic types as arguments or return type of convert/apply methods
 * todo
 * 1 generate additional fields on beans when generating convert/apply methods
 * 2 fix caveats
 */
public class CodeGenerationUtils {

    public enum SqlOperation {
        SELECT,
        UPDATE,
        INSERT
    }

    public static String WHERE_COLUMNS_SEPARATOR = ";";

    @Test
    @Ignore
    public void go() throws IOException, SQLException {
//        String fileName = "G:\\Projects\\\\src\\main\\java\\lol\\ormik\\bora\\commons\\services\\JdbcCallback.java";
//        applyConverts(fileName);
//        applyApplies(fileName);
        // callback after converts/applise, cause new fields may be generated
//        applyJdbcCallbacks(fileName, 4);
        System.out.println(generateSelect("sendform", "lol.lolpany.ormik", "Sendform",
                Integer.class, "b_code",
                new LinkedHashMap<String, Class<?>>() {{
                    put("b_agree", int.class);
                    put("b_plpay", int.class);
                }},
                new ArrayList<String>() {{
                    add("b_agree");
                    add("b_plpay");
                }}, "", SelectType.EXISTS));
//        System.out.println(generateSelect("forrclc", "lol.lolpany.ormik", Buyers.class,
//                new ArrayList<String>() {{
//                    add("b_mbc");
//                }}, " and b_act =1", SelectType.MULTIPLE));
    }
}
