import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This program demonstrates the use of the Function interface and streams in Java to process
 * a dataset of employees from a CSV file. It performs various operations such as concatenating
 * employee names and departments, calculating average salaries, and filtering employees based on age.
 * The program also showcases lazy evaluation, short-circuiting behavior, and optimizations
 * to improve efficiency and performance.
 */
public class EmployeeProcessor {

    public static void main(String[] args) {
        // Step 1: Read the dataset from the CSV file and store it in a collection
        List<Employee> employees = new ArrayList<>();
        try {
            employees = getEmployeeListFromCSV("employees.csv");
        } catch (IOException e) {
            System.err.println("Error reading the employee data: " + e.getMessage());
            return;
        }

        // Step 2: Create a Function that concatenates employee name and department
        Function<Employee, String> nameDepartmentConcatenator = employee ->
                employee.getName() + " - " + employee.getDepartment();

        // Step 3: Use streams to generate a new collection with the concatenated strings
        List<String> nameDepartmentList = employees.stream()
                .map(nameDepartmentConcatenator)
                .collect(Collectors.toList());

        System.out.println("Concatenated Name and Department:");
        nameDepartmentList.forEach(System.out::println);

        // Step 4: Calculate the average salary of all employees
        OptionalDouble averageSalary = employees.stream()
                .mapToDouble(Employee::getSalary)
                .average();

        if (averageSalary.isPresent()) {
            System.out.printf("%nAverage salary of all employees: %.2f%n", averageSalary.getAsDouble());
        } else {
            System.out.println("\nNo employee data available to calculate average salary.");
        }

        // Step 5: Filter employees whose age is above a certain threshold and process directly
        int ageThreshold = 30;

        System.out.println("\nEmployees above age " + ageThreshold + ":");
        employees.stream()
                .filter(employee -> employee.getAge() > ageThreshold)
                .forEach(employee ->
                        System.out.println(employee.getName() + ", Age: " + employee.getAge()));

        // Calculate the average salary of filtered employees directly from the stream
        OptionalDouble averageFilteredSalary = employees.stream()
                .filter(employee -> employee.getAge() > ageThreshold)
                .mapToDouble(Employee::getSalary)
                .average();

        if (averageFilteredSalary.isPresent()) {
            System.out.printf("%nAverage salary of employees above age %d: %.2f%n",
                    ageThreshold, averageFilteredSalary.getAsDouble());
        } else {
            System.out.println("\nNo employees found above the age threshold.");
        }

        // Demonstrate lazy evaluation and short-circuiting with anyMatch
        double highSalaryThreshold = 80000;
        boolean anyHighEarner = employees.stream()
                .anyMatch(employee -> {
                    System.out.println("Checking salary for " + employee.getName());
                    return employee.getSalary() > highSalaryThreshold;
                });

        if (anyHighEarner) {
            System.out.println("\nThere are employees earning more than $" + highSalaryThreshold);
        } else {
            System.out.println("\nNo employees earn more than $" + highSalaryThreshold);
        }

        // Demonstrate lazy evaluation with findFirst
        String targetDepartment = "Engineering";
        Optional<Employee> firstEngineer = employees.stream()
                .filter(employee -> {
                    System.out.println("Filtering for department Engineering: " + employee.getName());
                    return employee.getDepartment().equals(targetDepartment);
                })
                .findFirst();

        if (firstEngineer.isPresent()) {
            System.out.println("\nFirst employee in Engineering: " + firstEngineer.get().getName());
        } else {
            System.out.println("\nNo employees found in " + targetDepartment);
        }

        // Additional Feature: Group employees by department and list them
        Map<String, List<Employee>> employeesByDepartment = employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment));

        System.out.println("\nEmployees grouped by department:");
        employeesByDepartment.forEach((department, empList) -> {
            System.out.println("\nDepartment: " + department);
            empList.forEach(emp -> System.out.println(emp.getName()));
        });

        // Additional Feature: Find the highest salary in each department
        Map<String, Employee> highestSalaryByDept = employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparingDouble(Employee::getSalary)),
                                Optional::get)));

        System.out.println("\nHighest salary in each department:");
        highestSalaryByDept.forEach((department, emp) -> {
            System.out.printf("Department: %s, Employee: %s, Salary: %.2f%n",
                    department, emp.getName(), emp.getSalary());
        });

        // Additional Feature: Using parallel streams to improve performance
        double totalSalary = employees.parallelStream()
                .mapToDouble(Employee::getSalary)
                .sum();
        System.out.printf("%nTotal salary expenditure: %.2f%n", totalSalary);

        // Note: Parallel streams may improve performance for large datasets.
        // For small datasets, the overhead might not be worth it.
    }

    /**
     * Reads employee data from a CSV file and returns a list of Employee objects.
     *
     * @param fileName The name of the CSV file.
     * @return List of Employee objects.
     * @throws IOException If an I/O error occurs.
     */
    private static List<Employee> getEmployeeListFromCSV(String fileName) throws IOException {
        List<Employee> employees = new ArrayList<>();
        String line;
        String csvSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            // Skip the header line if present
            String header = br.readLine();
            if (header == null) {
                throw new IOException("The CSV file is empty.");
            }

            while ((line = br.readLine()) != null) {
                // Use comma as separator
                String[] data = line.split(csvSplitBy);

                if (data.length != 4) {
                    System.err.println("Invalid data format: " + line);
                    continue;
                }

                String name = data[0];
                int age;
                String department = data[2];
                double salary;

                try {
                    age = Integer.parseInt(data[1]);
                    salary = Double.parseDouble(data[3]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid number format: " + line);
                    continue;
                }

                Employee employee = new Employee(name, age, department, salary);
                employees.add(employee);
            }
        }

        return employees;
    }
}

/**
 * Employee class representing an employee with name, age, department, and salary.
 */
class Employee {
    private String name;
    private int age;
    private String department;
    private double salary;

    /**
     * Constructs an Employee object.
     *
     * @param name       Name of the employee.
     * @param age        Age of the employee.
     * @param department Department of the employee.
     * @param salary     Salary of the employee.
     */
    public Employee(String name, int age, String department, double salary) {
        this.name = name;
        this.age = age;
        this.department = department;
        this.salary = salary;
    }

    // Getters for the Employee attributes
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getDepartment() {
        return department;
    }

    public double getSalary() {
        return salary;
    }
}