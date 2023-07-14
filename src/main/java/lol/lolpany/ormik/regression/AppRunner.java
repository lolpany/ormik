package lol.lolpany.ormik.regression;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;

import static java.lang.Thread.sleep;

public class AppRunner extends EnvironmentBoundAction<Void> {


    final List<String> command;
    final List<Object> actions;

    public AppRunner(int envNumber, List<String> command, List<Object> actions) throws SQLException {
        super(envNumber);
        this.command = command;
        this.actions = actions;
    }

    @Override
    protected Void runOn(Integer envNumber) throws Exception {
        Process process = new ProcessBuilder(command).start();
        Robot r = new Robot();
        for (Object action : actions) {
            if (action instanceof Long) {
                sleep((Long) action);
            } else if (action instanceof Integer) {
                r.keyPress((Integer) action);
                sleep(100L);
            }
        }
        process.destroy();
        return null;
    }
}
