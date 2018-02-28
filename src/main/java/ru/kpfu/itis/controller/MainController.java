package ru.kpfu.itis.controller;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
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

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String renderMainPage() {
        return "main";
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String search(@RequestParam(required = false) String text,
                         RedirectAttributes redirectAttributes) throws IOException {
        redirectAttributes.addFlashAttribute("text", text);

        HashSet<String> results = new HashSet<>();
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
        ArrayList<String> filesList = new ArrayList<>();
        ApplicationContext appContext = new ClassPathXmlApplicationContext(new String[]{});
        Resource resource = appContext.getResource("information/invert/");
        File myFolder = resource.getFile();
        File[] files = myFolder.listFiles();
        for (File file : files) {
            filesList.add(file.getName());
        }

        //если слово всего одно
        if (words.length == 1) {
            ArrayList<String> list = new ArrayList<>();
            if (filesList.contains(words[0])) {
                String name = "information/invert/" + words[0];
                String line = getFileContent(name);
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
            ArrayList<String> resOfCon = new ArrayList<>();
            //Первый лист для сравнения
            List<String> firstList = new ArrayList<>();
            if (filesList.contains(words[0])) {
                String name = "information/invert/" + words[0];
                String line = getFileContent(name);
                String lineData[] = line.split(" ");
                firstList = Arrays.asList(lineData);
            }

            //работа со след словом
            for (int i = 1; i < words.length; i++) {
                if (filesList.contains(words[i])) {
                    String name = "information/invert/" + words[i];
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
        ApplicationContext appContext = new ClassPathXmlApplicationContext(new String[]{});
        Resource resource = appContext.getResource("information/index.txt");
        Scanner scanner = new Scanner(resource.getFile());
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String lineData[] = line.split(" ");
            index.put(lineData[0], lineData[1]);
        }
        return index;
    }

    private static String getFileContent(String filePath) throws IOException {
        ApplicationContext appContext = new ClassPathXmlApplicationContext(new String[]{});
        Resource resource = appContext.getResource(filePath);
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            if (br != null) try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
