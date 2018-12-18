import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainClass {
    public static void main(String[] args) throws IOException {
        Properties prop = new Properties();
        prop.load(new FileInputStream(new File("example.properties")));

        //Целевой файл
        File phoneBookFile = new File(prop.getProperty("targetFile"));
//        File phoneBookFile = new File("c:\\Users\\alekseev\\Desktop\\Телефоны ПМП.docx");
        if (phoneBookFile.exists()) phoneBookFile.delete();

        //Путь к каталогу с телефонным справочником

//        String pathDir = "z:\\!ТЕЛЕФОННЫЙ СПРАВОЧНИК и E-MAIL";
        File tBookFileDir = new File(prop.getProperty("basePhoneBookDirectory"));
        ArrayList<File> fileList = new ArrayList<>();   //список файлов из папки с телефонной книгой
        Collections.addAll(fileList,tBookFileDir.listFiles());

        ArrayList<Calendar> fileDateList = new ArrayList<>();

        //Подгружаем регулярку для поиска файлов с номерами телефонов и датами
        String regExpForFileNames = prop.getProperty("regExpForFileNames");
        //Ищем файлы с номерами телефонов и убираем ненужные
        filterFileList(fileList, regExpForFileNames);

        //Добавляем из fileList даты в dateList
        //Предполагается, что формат названий файлов жесткий и размер dateList и FileList совпадает.
        String regExpForDate = prop.getProperty("regExpForDate");
        addDateToDateListFromFileList(fileDateList, fileList, regExpForDate);

        //Ищем последнюю дату
        int lastDateIndex = searchLastDateIndex(fileDateList);

        //Записываем
        File lastBookFile = fileList.get(lastDateIndex);
        try {
            Files.copy(lastBookFile.toPath(),phoneBookFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //очистка списка файлов, оставляет только файлы с номерами телефонов
    private static void filterFileList(ArrayList<File> fileList, String regExp) {
        Pattern p = Pattern.compile(regExp);
//        Pattern p = Pattern.compile("^Телефоны ПМП на \\d{2}\\.\\d{2}\\.\\d{4}\\.docx$");

        for (int i = 0; i < fileList.size(); i++) {
            File file = fileList.get(i);
            String s = file.getName();
            Matcher m = p.matcher(s);
            if (!m.find()) {
                fileList.remove(file);  //Если не удовлетворяет - выкидываем из списка
                i--;
            }
        }
    }

    //Добавление в список дат даты из списка файлов
    private static void addDateToDateListFromFileList(ArrayList<Calendar> fileDateList, ArrayList<File> fileList, String regExp) {
//        Pattern datePat = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}");  // в найденных файлах вынимаем даты из названия
        Pattern datePat = Pattern.compile(regExp);  // в найденных файлах вынимаем даты из названия
        for (int i = 0; i< fileList.size(); i++) {
            Matcher dateMatch = datePat.matcher(fileList.get(i).getName());
            dateMatch.find();
            String date = dateMatch.group();
            String[] dateString = date.split("\\.");
            fileDateList.add(new GregorianCalendar(Integer.parseInt(dateString[2]), Integer.parseInt(dateString[1]), Integer.parseInt(dateString[0])));
        }
    }

    //Поиск последней даты
    private static int searchLastDateIndex(ArrayList<Calendar> fileDateList) {
        int lastDateIndex = 0;

        for (int i = 0; i < fileDateList.size() - 1; i++) {
            if (fileDateList.get(i).before(fileDateList.get(i+1))) {
                lastDateIndex = i+1;
            }
        }
        return lastDateIndex;
    }
}
