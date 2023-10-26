#!/Users/butlerb3/gh/filter-presentation/runjava.sh

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;
import java.lang.reflect.Field;

public class Demo {
    public static void main(String []args) {
        // START SEG2 OMIT
List<Transaction> transactions = Arrays.asList(
    new Transaction("Payment to Bailey"),
    new Transaction("Payment from Bailey"),
    new Transaction("Payment for Car"),
    new Transaction("Card Transaction")
);

FilterGroup<Transaction> TransactionFilter = new FilterGroup<>(Transaction.class);

for (Transaction Transaction : transactions) {
    if (TransactionFilter.filter(Transaction))
        System.out.println("Filtered: " + Transaction);
}
// END SEG2 OMIT
    }
}

// START SEG1 OMIT
public class Transaction {
    private String description;
    public Transaction(String description) {                   // OMIT
        this.description = description;                          // OMIT
    }                                              // OMIT
    public String toString() {                     // OMIT
        return "Transaction(name = " + this.description + ")"; // OMIT
    }                                              // OMIT
}
// END SEG1 OMIT

public abstract class Filter<T> implements Serializable {
    private final Class<T> classToFilter;
    protected final String fieldNameToFilter;

    public Filter(Class<T> classToFilter, String fieldNameToFilter) {
        this.classToFilter = classToFilter;
        this.fieldNameToFilter = fieldNameToFilter;
    }

    protected Field getReflectedField() {
        Field field;
        try {
            field = classToFilter.getDeclaredField(this.fieldNameToFilter);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(
                    "field '" + this.fieldNameToFilter + "' not found on class '" + classToFilter.getName() + "'", e);
        }

        field.setAccessible(true);
        return field;
    }

    public abstract boolean filter(T obj);
    public abstract void validate();
}

public class ContainsFilter<T> extends Filter<T> {
    private final String fieldToContainValue;

    public ContainsFilter(
        Class<T> classToFilter, String fieldNameToFilter, String fieldToContainValue) {
        super(classToFilter, fieldNameToFilter);
        this.fieldToContainValue = fieldToContainValue;
    }

    protected String getValue(T obj) {
        Object object;
        try {
            object = this.getReflectedField().get(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("illegal access of field", e);
        }
        return (String) object;
    }

    @Override
    public void validate() {
        Field field = this.getReflectedField();
        Class<?> fieldType = field.getType();
        if (!fieldType.isAssignableFrom(String.class)) {
            throw new IllegalArgumentException("field '" + this.fieldNameToFilter + "' is not string");
        }
    }

    @Override
    public boolean filter(T obj) {
        String valueInInstance = getValue(obj);
        if (valueInInstance == null) {
            return false;
        }
        return valueInInstance.toLowerCase().contains(this.fieldToContainValue.toLowerCase());
    }
}

public class FilterGroup<T> implements Serializable {
    private final ArrayList<Filter<T>> filterGroup = new ArrayList<>();
    private final Class<T> classToFilter;
    public FilterGroup(Class<T> classToFilter) {
        this.classToFilter = classToFilter;

        Filter<T> filter = new ContainsFilter<>(classToFilter, "description", "pay");
        this.filterGroup.add(filter);
    }
    public boolean filter(T object) {
        if (this.filterGroup.isEmpty()) {
            return true;
        }
        for (Filter<T> filter : this.filterGroup) {
            if (filter.filter(object)) {
                return true;
            }
        }
        return false;
    }
}
