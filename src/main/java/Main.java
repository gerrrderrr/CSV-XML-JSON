import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

        String fileCSV = "data.csv";
        List<Employee> list = parseCSV(columnMapping, fileCSV);
        String json = listToJson(list);
        writeString(json, "data.json");

        String fileXML = "data.xml";
        List<Employee> list2 = parseXML(fileXML);
        String json2 = listToJson(list2);
        writeString(json2, "data2.json");

        String json3 = readString("data.json");
        jsonToList(json3);
    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> list = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> cpms = new ColumnPositionMappingStrategy<>();
            cpms.setType(Employee.class);
            cpms.setColumnMapping(columnMapping);
            CsvToBean<Employee> ctb = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(cpms)
                    .build();
            list = ctb.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String listToJson(List<Employee> list) {
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        GsonBuilder gb = new GsonBuilder();
        Gson gson = gb.create();
        return gson.toJson(list, listType);
    }

    public static void writeString(String json, String fileName) {
        try (FileWriter fw = new FileWriter(fileName)) {
            fw.write(json);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Employee> parseXML(String fileName) throws ParserConfigurationException, IOException, SAXException {
        List<Employee> list = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(fileName));
        Node root = doc.getDocumentElement();
        read(list, root);
        return list;
    }

    private static void read(List<Employee> employees, Node node) {
        Map<String, String> employee = new HashMap<>();
        NodeList nodeList = node.getChildNodes();
        int l = nodeList.getLength();
        for (int i = 0; i < l; i++) {
            Node node1 = nodeList.item(i);
            if (i == l - 1 && l - 1 != 0 && !employee.isEmpty()) {
                employees.add(new Employee(Integer.parseInt(employee.get("id")),
                        employee.get("firstName"),
                        employee.get("lastName"),
                        employee.get("country"),
                        Integer.parseInt(employee.get("age"))));
            }
            if (Node.ELEMENT_NODE == node1.getNodeType()) {
                Element element = (Element) node1;
                NodeList values = element.getChildNodes();
                int x = values.getLength();
                if (x == 1) {
                    String value = element.getTextContent();
                    employee.put(node1.getNodeName(), value);
                }
                read(employees, node1);
            }
        }
    }

    public static String readString(String fileName) {
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            int i;
            while ((i = reader.read()) != -1) {
                text.append((char) i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    public static void jsonToList(String json) {
        JSONParser parser = new JSONParser();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        try {
            JSONArray obj = (JSONArray) parser.parse(json);
            for (Object o : obj) {
                System.out.println(gson.fromJson(o.toString(), Employee.class));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}