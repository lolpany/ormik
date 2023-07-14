package lol.lolpany.ormik.reinsertableBeans;

import javax.persistence.Transient;
import java.util.HashMap;
import java.util.Map;

/**
 * Solution of case, when its needed to process bean in memory and then insert it to db,
 * preserving all fields which are not processed explicitly.
 * If such 'other' field is added to table in future,
 * it will be preserved through 'select -> process -> insert' cycle.
 */
public class ReinsertableBean implements ISelectableBean, IInsertableBean {
    /**
     * Fields not used in code, indexed by column name.
     */
    @Transient
    public Map<String, Object> otherFields;

    public ReinsertableBean() {
        this.otherFields = new HashMap<>();
    }

    @Override
    public Map<String, Object> getOtherFields() {
        return otherFields;
    }

    @Override
    public void setOtherFields(Map<String, Object> map) {
        otherFields = map;
    }
}
