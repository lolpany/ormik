package lol.lolpany.ormik.dbAccess;

import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.sql.Date;

@Table(name = "employee")
@SequenceGenerator(name = "", sequenceName = "seq_employee")
public class Employee extends lol.lolpany.ormik.reinsertableBeans.ReinsertableBean implements IEmployee, ICoolEmployee {

    @Id
    public Integer id;
    public int department;
    public int sum;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getDepartment() {
        return department;
    }

    public void setDepartment(int department) {
        this.department = department;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }
}
