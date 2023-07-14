package lol.lolpany.ormik.regression;

import java.util.List;

public class HttpRequestComparator {

    public String runComparison(List<String> controlRequests, List<String> testedRequests) {
        if (controlRequests.size() != testedRequests.size()) {
            return "Different number of requests!";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < controlRequests.size(); i++) {
            if (!controlRequests.get(i).equals(testedRequests.get(i))) {
                result.append("request " + i + ":\ncontrol - " + controlRequests.get(i) + ", tested - "
                        + testedRequests.get(i));
            }
        }
        return result.toString();
    }
}
