package ru.kpfu.itis.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kpfu.itis.util.Porter;

import java.io.*;
import java.util.*;

/**
 * Created by Adel on 18.02.2018.
 */
@Controller
public class MainController extends BaseController {

    public static final String PATH = "information/invert/";

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String renderMainPage() {
        return "main";
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String search(@RequestParam(required = false) String text,
                         RedirectAttributes redirectAttributes) throws IOException {
        redirectAttributes.addFlashAttribute("text", text);

        HashSet<String> results = new HashSet<String>();
        Map<String, String> index = index();
        Porter porter = new Porter();

        if (text.isEmpty()) {
            redirectAttributes.addFlashAttribute("answer", "no results");
            return "redirect:/";
        }
        //преобразование слов в массив лемметированных слов
        String words[] = text.split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll("\\,|\\.|\\?|\\-|\\=|\\+|\\(|\\)|\\*|\\/|\\!|\"", "");
            words[i] = porter.stem(words[i]) + ".txt";
        }

        //создание списка с именами файлов
        ArrayList<String> filesList = new ArrayList<String>();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("information/invert.txt");
        Scanner scanner = new Scanner(inputStream);
        String line1 = scanner.nextLine();
        String lineData1[] = line1.split(" ");
        for (int i = 0; i < lineData1.length; i++) {
            filesList.add(lineData1[i] + ".txt");
        }


        //если слово всего одно
        if (words.length == 1) {
            ArrayList<String> list = new ArrayList<String>();
            if (filesList.contains(words[0])) {
                String name = PATH + words[0];
                inputStream = getClass().getClassLoader().getResourceAsStream(name);
                scanner = new Scanner(inputStream);
                String line = scanner.nextLine();
                String lineData[] = line.split(" ");
                for (String number : lineData) {
                    String name1 = number + ".txt";
                    if (index.containsKey(name1)) {
                        list.add(index.get(name1));
                    }
                }
                redirectAttributes.addFlashAttribute("results", list);
                return "redirect:/";
            } else {
                redirectAttributes.addFlashAttribute("answer", "no results");
            }
        }
        //если слов много
        else if (words.length > 1) {
            //Лист с конъюнкцией
            ArrayList<String> resOfCon = new ArrayList<String>();
            //Первый лист для сравнения
            List<String> firstList = new ArrayList<String>();
            if (filesList.contains(words[0])) {
                String name = PATH + words[0];
                String line = getFileContent(name);
                String lineData[] = line.split(" ");
                firstList = Arrays.asList(lineData);
            }
            //работа со след словом
            for (int i = 1; i < words.length; i++) {
                if (filesList.contains(words[i])) {
                    String name = PATH + words[i];
                    String line = getFileContent(name);
                    String lineData[] = line.split(" ");
                    List<String> list = Arrays.asList(lineData);

                    //проверка одинакового содержимого
                    if (i == 1) {
                        for (String elem : list) {
                            if (firstList.contains(elem)) {
                                resOfCon.add(elem);
                            }
                        }
                    }
                    if (i > 1 && !resOfCon.isEmpty()) {
                        for (String elem : list) {
                            if (!resOfCon.contains(elem)) {
                                redirectAttributes.addFlashAttribute("answer", "no results");
                                break;
                            }
                        }
                    }

                    //заполнение листа с урлами
                    for (String number : resOfCon) {
                        String name1 = number + ".txt";
                        if (index.containsKey(name1)) {
                            results.add(index.get(name1));
                        }
                    }
                    redirectAttributes.addFlashAttribute("results", results);
                } else {
                    redirectAttributes.addFlashAttribute("answer", "no results");
                    break;
                }

            }
        }
        return "redirect:/";
    }

    public Map<String, String> index() throws IOException {
        Map<String, String> index = new HashMap<String, String>();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("information/index.txt");
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String lineData[] = line.split(" ");
            index.put(lineData[0], lineData[1]);
        }
        return index;
    }

    private String getFileContent(String filePath) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
        Scanner scanner = new Scanner(inputStream);
        return scanner.nextLine();
    }
}
