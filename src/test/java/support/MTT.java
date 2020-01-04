package support;

import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class MTT {

    private File[] getFilesFromDirectory(String path) {
        File folder = new File(path);
        return folder.listFiles();
    }

    private LinkedHashMap<File, StringBuilder> getContentOfFiles(File[] files) {
        LinkedHashMap<File, StringBuilder> contentsOfFiles = new LinkedHashMap<>();
        for (File file : files) {
            try {
                StringBuilder content = new StringBuilder();
                BufferedReader br;
                br = new BufferedReader(new FileReader(file));
                String st;
                while ((st = br.readLine()) != null) {
                    if (st.trim().length() > 0) {
                        content.append(st).append("\n");
                    }
                }
                br.close();
                contentsOfFiles.put(file, content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return contentsOfFiles;
    }

    private LinkedHashMap<String, List<String>> getScenariosFromFileContent(List<String> linhasDeScenarios) {
        LinkedHashMap<String, List<String>> scenarios = new LinkedHashMap<>();
        for (String linhaDoScenario : linhasDeScenarios) {
            if (linhaDoScenario.trim().startsWith("Scenario:")) {
                List<String> metodos = new ArrayList<>();
                int i = linhasDeScenarios.indexOf(linhaDoScenario) + 1;
                while (!linhasDeScenarios.get(i).contains("Scenario:")) {
                    metodos.add(linhasDeScenarios.get(i).trim());
                    if (i >= linhasDeScenarios.size() - 1) {
                        break;
                    }
                    i++;
                }
                scenarios.put(linhaDoScenario.trim(), metodos);
            }
        }
        return scenarios;
    }

    /*private LinkedHashMap<String, List<String>> getMethodsFromFileContent(File[] arrayOfFilesStepDefs){
        LinkedHashMap<String, List<String>> stepdefs = new LinkedHashMap<>();
        for (File stepDefFile : arrayOfFilesStepDefs) {
            if (stepDefFile.isFile()) {
                //act = Class.forName(nomeClasse);
                List<String> contentList = new ArrayList<>();
                try {
                    BufferedReader brSd = new BufferedReader(new FileReader(stepDefFile));
                    String stSd;
                    while ((stSd = brSd.readLine()) != null) {
                        if (stSd.trim().length() > 0) {
                            contentList.add(stSd);
                        }
                    }
                    brSd.close();
                }catch (IOException ignored){}
                stepdefs.put(stepDefFile.getPath().split("\\\\java\\\\")[1].replace(".java", "").replace("\\", "."), contentList);
            }
        }
        return stepdefs;
    }*/

    private LinkedHashMap<Class<?>, Method> getFindMethod(LinkedHashMap<File, StringBuilder> stepdefsNoClass, LinkedHashMap<Class<?>, List<Method>> stepdefs, String step) {
        LinkedHashMap<Class<?>, Method> result = new LinkedHashMap<>();
        String rawMethodLine = "";
        for (Map.Entry<File, StringBuilder> entrySStepDef : stepdefsNoClass.entrySet()) {
            String[] linesContent = entrySStepDef.getValue().toString().split("[\n;]");
            String[] stepParts = step.split("\"");
            List<String> stepPartsToSearch = new ArrayList<>();
            for (int i = 0; i < stepParts.length; i++) {
                if (i % 2 == 0) {
                    stepPartsToSearch.add(stepParts[i]);
                }
            }
            for (String line : linesContent) {
                boolean found = true;
                for (String part : stepPartsToSearch) {
                    if (!line.contains(part)) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    //System.out.println("method: " + linesContent[Arrays.asList(linesContent).indexOf(line) + 1]);
                    rawMethodLine = linesContent[Arrays.asList(linesContent).indexOf(line) + 1];
                    break;
                }
            }
        }
        for (Map.Entry<Class<?>, List<Method>> entrySStepDef : stepdefs.entrySet()) {
            boolean found = false;
            String[] arrayOfStringsRawMethodLine = rawMethodLine.split("[ (]");
            for (String partRaw : arrayOfStringsRawMethodLine) {
                for (Method method : entrySStepDef.getValue()) {
                    if (method.getName().equals(partRaw)) {
                        result.put(entrySStepDef.getKey(), method);
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
            if (found) {
                break;
            }
        }
        return result;
    }

    @Test
    public void teste222() throws ClassNotFoundException {
        //............class....list of methods.........
        LinkedHashMap<Class<?>, List<Method>> stepdefs = new LinkedHashMap<>();

        File[] arrayOfFilesStepDefs = getFilesFromDirectory(System.getProperty("user.dir") + "/src/test/java/suites/mtta/stepdefs/");

        //............file....content.........
        LinkedHashMap<File, StringBuilder> stepdefsNoClass = getContentOfFiles(arrayOfFilesStepDefs);
        for (Map.Entry<File, StringBuilder> entrySStepDef : stepdefsNoClass.entrySet()) {
            Class<?> act = Class.forName(entrySStepDef.getKey().getPath().split("\\\\java\\\\")[1].replace(".java", "").replace("\\", "."));
            stepdefs.put(act, Arrays.asList(act.getMethods()));
        }

        File[] arrayOfFilesFeatures = getFilesFromDirectory(System.getProperty("user.dir") + "/src/test/java/suites/mtta/features/");
        LinkedHashMap<File, StringBuilder> contentsOfFiles = getContentOfFiles(arrayOfFilesFeatures);

        //foreach feature file
        for (Map.Entry<File, StringBuilder> entryContentsOfFiles : contentsOfFiles.entrySet()) {

            //............file....list of scenarios.........
            LinkedHashMap<String, List<String>> scenarios = getScenariosFromFileContent(Arrays.asList(entryContentsOfFiles.getValue().toString().split("[\n;]")));
            LinkedHashMap<Integer, Boolean> openThreads = new LinkedHashMap<>();
            int threadCount = 0;

            for (Map.Entry<String, List<String>> entryScenarios : scenarios.entrySet()) {
                threadCount++;
                openThreads.put(threadCount, true);
                int finalThreadCount = threadCount;

                new Thread(() -> {
                    int myNumber = finalThreadCount;
                    for (String step : entryScenarios.getValue()) {
                        String[] stepParts = step.split("\"");
                        List<Object> stepParameters = new ArrayList<>();
                        for (int i = 0; i < stepParts.length; i++) {
                            if (i % 2 != 0) {
                                stepParameters.add(stepParts[i]);
                            }
                        }
                        Object classInstance = null;
                        LinkedHashMap<Class<?>, Method> result = getFindMethod(stepdefsNoClass, stepdefs, step);
                        for (Map.Entry<Class<?>, Method> entryResult : result.entrySet()) {

                            //entryResult.getValue().invoke(entryResult.getKey().newInstance(), stepParameters.toArray());
                            try {
                                if(classInstance == null){
                                    classInstance = entryResult.getKey().newInstance();
                                }
                                //System.out.println(entryResult.getKey().getName()+" - "+entryResult.getValue());
                                entryResult.getValue().invoke(classInstance, stepParameters.toArray());
                            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                    openThreads.put(myNumber, false);
                }).start();
            }
            boolean thereIsNoMoreOpenedThreads = true;
            while (thereIsNoMoreOpenedThreads) {
                thereIsNoMoreOpenedThreads = false;
                for (Map.Entry<Integer, Boolean> entryOpenThreads : openThreads.entrySet()) {
                    if (entryOpenThreads.getValue()) {
                        thereIsNoMoreOpenedThreads = true;
                    }
                }
            }
        }





/*
        NavigableMap<String, String> stepdefs = new TreeMap<>();
        for (File arquivoStepDef : listOfFilesSd) {
            BufferedReader brSd = new BufferedReader(new FileReader(arquivoStepDef));
            StringBuilder contentSd = new StringBuilder();
            String stSd;
            while ((stSd = brSd.readLine()) != null) {
                if (stSd.trim().length() > 0) {
                    contentSd.append(stSd).append("\n");
                }
            }
            br.close();
            stepdefs.put(arquivoStepDef.toString(), contentSd.toString());
        }
        String metodo1;
        String nomeMetodo;
        List<Object> parameter;
        System.out.println(nomeClasse);
        for (Map.Entry<String, List<String>> entry : scenarios.entrySet()) {
            List<String> metodos = entry.getValue();
            for (String metodo : metodos) {
                metodo = metodo.replace("Given", "").replace("When", "").replace("And", "").replace("Then", "");
                metodo1 = metodo.trim();
                for (Map.Entry<String, String> entrySd : stepdefs.entrySet()) {
                    String[] stepdef = entrySd.getValue().split("\n");
                    Pattern pattern = Pattern.compile(".*\\\"(.*)\\\".*");
                    Matcher matcher = pattern.matcher(metodo1);
                    parameter = new ArrayList<>();
                    while (matcher.find()) {
                        parameter.add(matcher.group(1));
                        for (Object p : parameter) {
                            metodo1 = metodo1.replace((String) p, "");
                            metodo1 = metodo1.replaceAll("\"\"", "{string}");
                        }
                        matcher = pattern.matcher(metodo1);
                    }
                    Collections.reverse(parameter);
                    Pattern patternParameter = Pattern.compile(".*\\((.*)\\).*");
                    Matcher matcherParameter = patternParameter.matcher(metodo1);
                    while (matcherParameter.find()) {
                        parameter.add(matcherParameter.group(1));
                        for (Object p : parameter) {
                            metodo1 = metodo1.replace((String) p, "");
                            metodo1 = metodo1.replaceAll("\"\"", "{string}");
                        }
                        matcherParameter = patternParameter.matcher(metodo1);
                    }
                    for (String stepdefLinha : stepdef) {
                        if (stepdefLinha.contains(metodo1)) {
                            nomeMetodo = Arrays.asList(stepdef).get(Arrays.asList(stepdef).indexOf(stepdefLinha) + 1)
                                    .replace("public", "")
                                    .replace("void", "")
                                    .replace("()", "")
                                    .replace("{", "")
                                    .trim();
                            String[] nomeMetodoArgs = nomeMetodo.split("\\(");
                            nomeMetodo = nomeMetodoArgs[0];
                            System.out.println("metodo: " + nomeMetodo);
                            Method[] methods = act != null ? act.getMethods() : new Method[0];
                            for (Method met : methods) {
                                if (met.getName().equals(nomeMetodo)) {
                                    System.out.println("met.getName() " + met.getName());
                                    int parameterCount = met.getParameterCount();
                                    System.out.println("met.getParameterCount() " + parameterCount);
                                    System.out.println("met.getParameterTypes() " + Arrays.toString(met.getParameterTypes()));
                                    if (parameterCount >= 1) {
                                        met.invoke(act.newInstance(), parameter.toArray());
                                    } else {
                                        met.invoke(act.newInstance());
                                    }
                                    System.out.println("INVOCADO");
                                    System.out.println("===========================================================================");
                                }
                            }
                        }
                    }
                    break;
                }
            }
            break;
        }*/
    }

    private final static int[] localMaximumThreads = {1};

    public void run(String stepDefs, String features, int maxInstances) {
        //............class....list of methods.........
        LinkedHashMap<Class<?>, List<Method>> stepdefs = new LinkedHashMap<>();

        File[] arrayOfFilesStepDefs = getFilesFromDirectory(System.getProperty("user.dir") + stepDefs);

        //............file....content.........
        LinkedHashMap<File, StringBuilder> stepdefsNoClass = getContentOfFiles(arrayOfFilesStepDefs);
        for (Map.Entry<File, StringBuilder> entrySStepDef : stepdefsNoClass.entrySet()) {
            Class<?> act = null;
            try {
                act = Class.forName(entrySStepDef.getKey().getPath().split("\\\\java\\\\")[1].replace(".java", "").replace("\\", "."));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            stepdefs.put(act, Arrays.asList(act != null ? act.getMethods() : new Method[0]));
        }

        File[] arrayOfFilesFeatures = getFilesFromDirectory(System.getProperty("user.dir") + features);
        LinkedHashMap<File, StringBuilder> contentsOfFiles = getContentOfFiles(arrayOfFilesFeatures);

        //foreach feature file
        for (Map.Entry<File, StringBuilder> entryContentsOfFiles : contentsOfFiles.entrySet()) {

            //............file....list of scenarios.........
            LinkedHashMap<String, List<String>> scenarios = getScenariosFromFileContent(Arrays.asList(entryContentsOfFiles.getValue().toString().split("[\n;]")));
            LinkedHashMap<Integer, Boolean> openThreads = new LinkedHashMap<>();
            int threadCount = 0;
            localMaximumThreads[0] = maxInstances;

            for (Map.Entry<String, List<String>> entryScenarios : scenarios.entrySet()) {
                threadCount++;
                openThreads.put(threadCount, true);
                while (localMaximumThreads[0] == 0){
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                int finalThreadCount = threadCount;

                localMaximumThreads[0]--;
                new Thread(() -> {
                    int myNumber = finalThreadCount;
                    for (String step : entryScenarios.getValue()) {
                        String[] stepParts = step.split("\"");
                        List<Object> stepParameters = new ArrayList<>();
                        for (int i = 0; i < stepParts.length; i++) {
                            if (i % 2 != 0) {
                                stepParameters.add(stepParts[i]);
                            }
                        }
                        Object classInstance = null;
                        LinkedHashMap<Class<?>, Method> result = getFindMethod(stepdefsNoClass, stepdefs, step);
                        for (Map.Entry<Class<?>, Method> entryResult : result.entrySet()) {

                            //entryResult.getValue().invoke(entryResult.getKey().newInstance(), stepParameters.toArray());
                            try {
                                if(classInstance == null){
                                    classInstance = entryResult.getKey().newInstance();
                                }
                                //System.out.println(entryResult.getKey().getName()+" - "+entryResult.getValue());
                                entryResult.getValue().invoke(classInstance, stepParameters.toArray());
                            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                    openThreads.put(myNumber, false);
                    localMaximumThreads[0]++;
                }).start();
            }
            boolean thereIsNoMoreOpenedThreads = true;
            while (thereIsNoMoreOpenedThreads) {
                thereIsNoMoreOpenedThreads = false;
                for (Map.Entry<Integer, Boolean> entryOpenThreads : openThreads.entrySet()) {
                    if (entryOpenThreads.getValue()) {
                        thereIsNoMoreOpenedThreads = true;
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }





/*
        NavigableMap<String, String> stepdefs = new TreeMap<>();
        for (File arquivoStepDef : listOfFilesSd) {
            BufferedReader brSd = new BufferedReader(new FileReader(arquivoStepDef));
            StringBuilder contentSd = new StringBuilder();
            String stSd;
            while ((stSd = brSd.readLine()) != null) {
                if (stSd.trim().length() > 0) {
                    contentSd.append(stSd).append("\n");
                }
            }
            br.close();
            stepdefs.put(arquivoStepDef.toString(), contentSd.toString());
        }
        String metodo1;
        String nomeMetodo;
        List<Object> parameter;
        System.out.println(nomeClasse);
        for (Map.Entry<String, List<String>> entry : scenarios.entrySet()) {
            List<String> metodos = entry.getValue();
            for (String metodo : metodos) {
                metodo = metodo.replace("Given", "").replace("When", "").replace("And", "").replace("Then", "");
                metodo1 = metodo.trim();
                for (Map.Entry<String, String> entrySd : stepdefs.entrySet()) {
                    String[] stepdef = entrySd.getValue().split("\n");
                    Pattern pattern = Pattern.compile(".*\\\"(.*)\\\".*");
                    Matcher matcher = pattern.matcher(metodo1);
                    parameter = new ArrayList<>();
                    while (matcher.find()) {
                        parameter.add(matcher.group(1));
                        for (Object p : parameter) {
                            metodo1 = metodo1.replace((String) p, "");
                            metodo1 = metodo1.replaceAll("\"\"", "{string}");
                        }
                        matcher = pattern.matcher(metodo1);
                    }
                    Collections.reverse(parameter);
                    Pattern patternParameter = Pattern.compile(".*\\((.*)\\).*");
                    Matcher matcherParameter = patternParameter.matcher(metodo1);
                    while (matcherParameter.find()) {
                        parameter.add(matcherParameter.group(1));
                        for (Object p : parameter) {
                            metodo1 = metodo1.replace((String) p, "");
                            metodo1 = metodo1.replaceAll("\"\"", "{string}");
                        }
                        matcherParameter = patternParameter.matcher(metodo1);
                    }
                    for (String stepdefLinha : stepdef) {
                        if (stepdefLinha.contains(metodo1)) {
                            nomeMetodo = Arrays.asList(stepdef).get(Arrays.asList(stepdef).indexOf(stepdefLinha) + 1)
                                    .replace("public", "")
                                    .replace("void", "")
                                    .replace("()", "")
                                    .replace("{", "")
                                    .trim();
                            String[] nomeMetodoArgs = nomeMetodo.split("\\(");
                            nomeMetodo = nomeMetodoArgs[0];
                            System.out.println("metodo: " + nomeMetodo);
                            Method[] methods = act != null ? act.getMethods() : new Method[0];
                            for (Method met : methods) {
                                if (met.getName().equals(nomeMetodo)) {
                                    System.out.println("met.getName() " + met.getName());
                                    int parameterCount = met.getParameterCount();
                                    System.out.println("met.getParameterCount() " + parameterCount);
                                    System.out.println("met.getParameterTypes() " + Arrays.toString(met.getParameterTypes()));
                                    if (parameterCount >= 1) {
                                        met.invoke(act.newInstance(), parameter.toArray());
                                    } else {
                                        met.invoke(act.newInstance());
                                    }
                                    System.out.println("INVOCADO");
                                    System.out.println("===========================================================================");
                                }
                            }
                        }
                    }
                    break;
                }
            }
            break;
        }*/
    }
}
