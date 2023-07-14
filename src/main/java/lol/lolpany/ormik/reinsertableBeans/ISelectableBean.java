package lol.lolpany.ormik.reinsertableBeans;

import java.util.Map;

public interface ISelectableBean extends IBean {
    void setOtherFields(Map<String, Object> map);
}
