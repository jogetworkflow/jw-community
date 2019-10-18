package org.joget.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.Buffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.lib.SimpleTensorFlowAIDecisionPlugin;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppResourceUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.service.FileUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.tensorflow.DataType;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;

/**
 * Utility for the TensorFlow Java API using pre-trained models.
 */
public class TensorFlowUtil {
    
    public static Map<String, TensorFlowInput> defaultInputClasses = null;
    public static Map<String, TensorFlowPostProcessing> defaultPostClasses = null;
    
    public static boolean isValidURL(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
    
    public static InputStream getInputStream(String filename, String formId, String recordId) {
        try {
            if (formId != null && !formId.isEmpty()) {
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
                File file = FileUtil.getFile(filename, appService.getFormTableName(appDef, formId), recordId);
                return new FileInputStream(file);
            } else if (isValidURL(filename)) {
                return new URL(filename).openStream();
            } else {
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                return new FileInputStream(AppResourceUtil.getFile(appDef.getAppId(), appDef.getVersion().toString(), filename));
            }
        } catch (Exception e) {
            LogUtil.debug(TensorFlowUtil.class.getName(), "Fail to open " + filename);
        }
        return null;
    }
    
    public static DataType getDataType(String name) {
        if ("Integer".equals(name)) {
            return DataType.INT32;
        } else if ("Long".equalsIgnoreCase(name)) {
            return DataType.INT64;
        } else if ("Double".equalsIgnoreCase(name)) {
            return DataType.DOUBLE;
        } else if ("UInt8".equalsIgnoreCase(name)) {
            return DataType.UINT8;
        } else if ("Boolean".equalsIgnoreCase(name)) {
            return DataType.BOOL;
        } else if ("String".equalsIgnoreCase(name)) {
            return DataType.STRING;
        }
        return DataType.FLOAT; 
    }
    
    public static Map<String, Integer> getDictionaryJson(InputStream inputStream) throws IOException {
        Map<String, Integer> dictionaryMap = null;
        try {
            String vocabJson = IOUtils.toString(inputStream);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Type type = new TypeToken<Map<String, Integer>>(){}.getType();
            dictionaryMap = gson.fromJson(vocabJson, type);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {}
            }
        }
        return dictionaryMap;
    }
    
    public static Map<String, Integer> getDictionaryCsv(InputStream inputStream) throws IOException {
        String[] delimeters = new String[] {",", "\t", ";", "|", " "};
        Map<String, Map<String, Integer>> dictionaryMap = new HashMap<String, Map<String, Integer>>();
        for (String delimter : delimeters) {
            dictionaryMap.put(delimter, new HashMap<String, Integer>());
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = "";
            String[] temp;
            String label;
            String value;
            while ((line = br.readLine()) != null) {
                for (String delimter : delimeters) {
                    temp = line.split(delimter);
                    if (temp.length == 2) {
                        label = temp[0];
                        value = temp[1];
                        if (label.startsWith("\"") && label.endsWith("\"")) {
                            label = label.substring(1, label.length() -1);
                        }
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = label.substring(1, value.length() -1);
                        }
                        try {
                            dictionaryMap.get(delimter).put(label, Integer.parseInt(value));
                        } catch (Exception err) {
                            //ignore
                        }
                    }
                }
            }
        }finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {}
            }
        }
        String theDelimter = "";
        int size = 0;
        for (String delimter : delimeters) {
            if (dictionaryMap.get(delimter).size() > size) {
                theDelimter = delimter;
                size = dictionaryMap.get(delimter).size();
            }
        }
        return dictionaryMap.get(theDelimter);
    }
    
    public static Tensor imageInput(InputStream inputStream, String imageType, Integer height, Integer width, Float mean, Float scale, String type) throws IOException {
        DataType dataType = getDataType(type);
        try (Graph g = new Graph()) {
            byte[] imageBytes = IOUtils.toByteArray(inputStream);
            GraphBuilder b = new GraphBuilder(g);
            final Output input = b.constant("input", imageBytes);
            Output imageOutput = b.decodeJpeg(input, 3);
            LogUtil.debug(TensorFlowUtil.class.getName(), "Decoded image " + imageType);
            Output output = null;
            
            if (height != null && width != null) {
                LogUtil.debug(TensorFlowUtil.class.getName(), "Normalizing image " + imageType);
                if (mean == null) {
                    mean = 1f;
                }
                if (scale == null) {
                    scale = 1f;
                }
                output = b.cast(
                            b.div(
                                b.sub(
                                    b.resizeBilinear(
                                        b.expandDims(
                                            imageOutput,
                                            b.constant("make_batch", 0)),
                                        b.constant("size", new int[]{height, width})),
                                    b.constant("mean", mean)),
                                b.constant("scale", scale)),
                            dataType);
            } else {
                output = b.cast(
                            b.expandDims(
                                imageOutput, 
                                b.constant("make_batch", 0)), 
                            dataType);
            }
            
            try (Session s = new Session(g)) {
                Tensor result = s.runner().fetch(output.op().name()).run().get(0);
                LogUtil.debug(TensorFlowUtil.class.getName(), "image " + imageType);
                return result;
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
    
    public static Tensor textInput(String text, InputStream dictInputStream, String fileType, int maxLength, String type, Boolean fillBack) throws IOException {
        text = text.toLowerCase();
        ArrayList<String> words = new ArrayList<>(Arrays.asList(text.split("\\s+"))); // 
        Map<String, Integer> dictionary = null;
        
        if ("json".equalsIgnoreCase(fileType)) {
            dictionary = getDictionaryJson(dictInputStream);
        } else {
            dictionary = getDictionaryCsv(dictInputStream);
        }
        
        Buffer buffer = null;
        if ("Integer".equalsIgnoreCase(type) || "UInt8".equalsIgnoreCase(type)) {
            buffer = IntBuffer.allocate(maxLength);
        } else if ("Long".equalsIgnoreCase(type)) {
            buffer = LongBuffer.allocate(maxLength);
        } else if ("Double".equalsIgnoreCase(type)) {
            buffer = DoubleBuffer.allocate(maxLength);
        } else {
            buffer = FloatBuffer.allocate(maxLength);
        }
        
        int index = 0;
        int start = 0;
        
        if (fillBack != null && fillBack) {
            start = maxLength - Math.min(maxLength, words.size());
        }
        
        for (String word : words) {
            int idx = 0;
            if (dictionary.containsKey(word)) {
                idx = dictionary.get(word);
            }
            
            if ("Integer".equalsIgnoreCase(type) || "UInt8".equalsIgnoreCase(type)) {
                ((IntBuffer) buffer).put(start + index, idx);
            } else if ("Long".equalsIgnoreCase(type)) {
                ((LongBuffer) buffer).put(start + index, idx);
            } else if ("Double".equalsIgnoreCase(type)) {
                ((DoubleBuffer) buffer).put(start + index, idx);
            } else {
                ((FloatBuffer) buffer).put(start + index, idx);
            }
            
            if (start + index == maxLength - 1) {
                break;
            }
            
            index += 1;
        }

        if ("Integer".equalsIgnoreCase(type) || "UInt8".equalsIgnoreCase(type)) {
            return Tensor.create(new long[] {1, maxLength}, (IntBuffer) buffer);
        } else if ("Long".equalsIgnoreCase(type)) {
            return Tensor.create(new long[] {1, maxLength}, (LongBuffer) buffer);
        } else if ("Double".equalsIgnoreCase(type)) {
            return Tensor.create(new long[] {1, maxLength}, (DoubleBuffer) buffer);
        } else {
            return Tensor.create(new long[] {1, maxLength}, (FloatBuffer) buffer);
        }
    }
    
    public static Tensor numbersInput(String text, String type) throws IOException {
        try {
            String[] values = text.split(";");
            if (values.length == 1) {
                if ("Integer".equalsIgnoreCase(type) || "UInt8".equalsIgnoreCase(type)) {
                    return Tensors.create(Integer.parseInt(values[0]));  
                } else if ("Long".equalsIgnoreCase(type)) {
                    return Tensors.create(Long.parseLong(values[0]));  
                } else if ("Double".equalsIgnoreCase(type)) {
                    return Tensors.create(Double.parseDouble(values[0]));  
                } else if ("Float".equalsIgnoreCase(type)) {
                    return Tensors.create(Float.parseFloat(values[0])); 
                }
            } else {
                if ("Integer".equalsIgnoreCase(type) || "UInt8".equalsIgnoreCase(type)) {
                    int[][] numbers = new int[1][values.length];
                    for (int i=0; i<values.length; i++) {
                        numbers[0][i] = Integer.parseInt(values[i]);
                    }
                    return Tensors.create(numbers);  
                } else if ("Long".equalsIgnoreCase(type)) {
                    long[][] numbers = new long[1][values.length];
                    for (int i=0; i<values.length; i++) {
                        numbers[0][i] = Long.parseLong(values[i]);
                    }
                    return Tensors.create(numbers);  
                } else if ("Double".equalsIgnoreCase(type)) {
                    double[][] numbers = new double[1][values.length];
                    for (int i=0; i<values.length; i++) {
                        numbers[0][i] = Double.parseDouble(values[i]);
                    }
                    return Tensors.create(numbers);   
                } else if ("Float".equalsIgnoreCase(type)) {
                    float[][] numbers = new float[1][values.length];
                    for (int i=0; i<values.length; i++) {
                        numbers[0][i] = Float.parseFloat(values[i]);
                    }
                    return Tensors.create(numbers);  
                }
            }
        } catch (Exception e) {}
        return null;
    }
    
    public static Tensor booleanInput(String value) throws IOException {
        Tensor inputTensor = Tensors.create(Boolean.parseBoolean(value));    
        return inputTensor;
    }
    
    /**
     * Run inference on a simple pre-trained TensorFlow model, where each output is a rank 1 tensor.
     * @param graphInputStream The TensorFlow model
     * @param inputTensorMap Map of input operation name -> input Tensor
     * @param outputNames Array of output operation names to fetch
     * @return Map of output operation name -> output result as a float array
     */
    public static Map<String, float[]> executeSimpleTensorFlowModel(InputStream graphInputStream, Map<String, Tensor> inputTensorMap, String[] outputNames) throws IOException {
        Map<String, float[]> resultMap = new LinkedHashMap<>();
        Map<String, Tensor> tensorMap = executeTensorFlowModel(graphInputStream, inputTensorMap, outputNames);
        for (String operationName: tensorMap.keySet()) {
            try (Tensor tensor = tensorMap.get(operationName)) {
                long[] rshape = tensor.shape();
                int size = 1;
                for (int i = 0; i < rshape.length; i++) {
                    size *= rshape[i];
                }
                float[] result = new float[size];
                if (tensor.dataType().equals(DataType.INT32) || tensor.dataType().equals(DataType.INT64) || tensor.dataType().equals(DataType.UINT8)) {
                    int[] tempResult = new int[size];
                    IntBuffer intBuffer = IntBuffer.wrap(tempResult);
                    tensor.writeTo(intBuffer);
                    for (int i = 0; i < size; i++) {
                        result[i] = tempResult[i];
                    }
                } else {
                    FloatBuffer floatBuffer = FloatBuffer.wrap(result);
                    tensor.writeTo(floatBuffer);
                }
                resultMap.put(operationName, result);
            }
        }
        return resultMap;
    }
    
    /**
     * Run inference on a pre-trained TensorFlow model
     * @param graphInputStream The TensorFlow model
     * @param inputTensorMap Map of input operation name -> input Tensor
     * @param outputNames Array of output operation names to fetch
     * @return Map of output operation name -> Tensor. Close each tensor after use to avoid memory leaks.
     */
    public static Map<String, Tensor> executeTensorFlowModel(InputStream graphInputStream, Map<String, Tensor> inputTensorMap, String[] outputNames) throws IOException {
        byte[] graphDef = IOUtils.toByteArray(graphInputStream);
        
        try {
            Map<String, Tensor> resultMap = new LinkedHashMap<>();
            try (Graph g = new Graph()) {
                LogUtil.debug(TensorFlowUtil.class.getName(), "Importing graph model");
                g.importGraphDef(graphDef);
                LogUtil.debug(TensorFlowUtil.class.getName(), "Running inference with input tensors " + inputTensorMap);
                try (Session s = new Session(g)) {
                    Session.Runner runner = s.runner();
                    for (String key: inputTensorMap.keySet()) {
                        runner.feed(key, inputTensorMap.get(key));
                    }
                    for (String key: outputNames) {
                        runner.fetch(key);
                    }
                    List<Tensor<?>> outputTensors = runner.run();
                    int i=0;
                    for (Tensor tensor: outputTensors) {
                        String operationName = outputNames[i];
                        i++;
                        resultMap.put(operationName, tensor);
                    }
                    return resultMap;
                }
            }
        } finally {
            if (graphInputStream != null) {
                try {
                    graphInputStream.close();
                } catch (Exception e) {}
            }
        }
    }  
    
    public static Map<Float, String> getSortedLabelResultMap(InputStream labelInputStream, float[] results, Float threshold) throws IOException {
        Map<Float, String> resultMap = new TreeMap<>(Collections.reverseOrder());
        try {
            List<String> labels = IOUtils.readLines(labelInputStream);
            if (threshold == null || threshold < 0) {
                threshold = 0.01f;
            }
            int i=0;
            for (String label: labels) {
                float probability = results[i];
                if (probability > threshold) {
                    resultMap.put(probability, label);
                }
                i++;
            }
        } finally {
            if (labelInputStream != null) {
                labelInputStream.close();
            }
        }
        return resultMap;
    }
    
    public static List<String> getValueToLabelList(InputStream labelInputStream, float[] results, Integer numberOfValues, Boolean unique, Float threshold, float[] scores) throws IOException {
        List<String> resultLabels = new ArrayList<String>();
        try {
            List<String> labels = IOUtils.readLines(labelInputStream);
            if (numberOfValues == null) {
                numberOfValues = results.length;
            }
            for (int i=0; i < numberOfValues; i++) {
                String label = labels.get((int)(results[i] - 1));
                boolean pass = true;
                if (threshold != null && scores != null) {
                    pass = scores[i] > threshold;
                }
                if (pass && (unique == null || !unique || (unique && !resultLabels.contains(label)))) {
                    resultLabels.add(label);
                }
            }
        } finally {
            if (labelInputStream != null) {
                labelInputStream.close();
            }
        }
        return resultLabels;
    }
    
    public static float getEuclideanDistance(float[] a, float[] b) {
        float diff_square_sum = 0.0f;
        for (int i = 0; i < a.length; i++) {
            diff_square_sum += (a[i] - b[i]) * (a[i] - b[i]);
        }
        return (float)Math.sqrt(diff_square_sum);
    }   

    public static String outputToJson(float[] output) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(output);
        return json;
    }
    
    public static String outputToJson(Object output) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(output);
        return json;
    }
    
    public static class GraphBuilder {

        GraphBuilder(Graph g) {
            this.g = g;
        }

        Output div(Output x, Output y) {
            return binaryOp("Div", x, y);
        }

        Output sub(Output x, Output y) {
            return binaryOp("Sub", x, y);
        }

        Output resizeBilinear(Output images, Output size) {
            return binaryOp("ResizeBilinear", images, size);
        }

        Output expandDims(Output input, Output dim) {
            return binaryOp("ExpandDims", input, dim);
        }

        Output cast(Output value, DataType dtype) {
            return g.opBuilder("Cast", "Cast").addInput(value).setAttr("DstT", dtype).build().output(0);
        }

        Output decodeJpeg(Output contents, long channels) {
            return g.opBuilder("DecodeJpeg", "DecodeJpeg")
                    .addInput(contents)
                    .setAttr("channels", channels)
                    .build()
                    .output(0);
        }

        Output constant(String name, Object value) {
            try (Tensor t = Tensor.create(value)) {
                return g.opBuilder("Const", name)
                        .setAttr("dtype", t.dataType())
                        .setAttr("value", t)
                        .build()
                        .output(0);
            }
        }

        private Output binaryOp(String type, Output in1, Output in2) {
            return g.opBuilder(type, type).addInput(in1).addInput(in2).build().output(0);
        }

        private final Graph g;
    }
    
    public static String getEditorScript(HttpServletRequest request, HttpServletResponse response) {
        String forms = "[]";
        try {
            JSONArray formArray = new JSONArray();
            
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();
            FormDefinitionDao formDefinitionDao = (FormDefinitionDao) AppUtil.getApplicationContext().getBean("formDefinitionDao");
            Collection<FormDefinition> formDefinitionList = formDefinitionDao.getFormDefinitionList(null, appDef, null, null, null, null);
            if (formDefinitionList != null && !formDefinitionList.isEmpty()) {
                for (FormDefinition f : formDefinitionList) {
                    JSONObject fo = new JSONObject();
                    fo.put("value", f.getId());
                    fo.put("label", f.getName());
                    
                    formArray.put(fo);
                }
            }
            
            forms = formArray.toString();
        } catch(Exception e) {}
        
        String inputs = "{}";
        Map<String, TensorFlowInput> inputClasses = getInputClasses();
        try {
            JSONObject jsonInputs = new JSONObject();
            
            if (!inputClasses.isEmpty()) {
                for (String key : inputClasses.keySet()) {
                    JSONObject jo = new JSONObject();
                    TensorFlowInput i = inputClasses.get(key);
                    jo.put("label", i.getLabel());
                    jo.put("ui", i.getUI());
                    jo.put("description", i.getDescription());
                    jo.put("initScript", i.getInitScript());
                    
                    jsonInputs.put(key, jo);
                }
            }
            
            inputs = jsonInputs.toString();
        } catch(Exception e) {}
        
        String posts = "{}";
        Map<String, TensorFlowPostProcessing> postClasses = getPostProcessingClasses();
        try {
            JSONObject jsonPosts = new JSONObject();
            
            if (!postClasses.isEmpty()) {
                for (String key : postClasses.keySet()) {
                    JSONObject jo = new JSONObject();
                    TensorFlowPostProcessing i = postClasses.get(key);
                    jo.put("label", i.getLabel());
                    jo.put("ui", i.getUI());
                    jo.put("description", i.getDescription());
                    jo.put("initScript", i.getInitScript());
                    
                    jsonPosts.put(key, jo);
                }
            }
            
            posts = jsonPosts.toString();
        } catch(Exception e) {}
        
        return AppUtil.readPluginResource(SimpleTensorFlowAIDecisionPlugin.class.getName(), "/properties/app/tensorflowEditor.js", new String[]{forms, inputs, posts}, false, null);
    }
    
    public static Map<String, Object> getEditorResults(Map config, String processId, Map<String, String> variables) {
        Map<String, Object> tfvariables = new HashMap<String, Object>();
        Map<String, Object> tempDataHolder = new HashMap<String, Object>();
        
        if (config != null) {
            try {
                Object[] sessions = (Object[]) config.get("sessions");
                if (sessions != null && sessions.length > 0) {
                    for (Object sessionObj : sessions) {
                        Map session = (Map) sessionObj;
                        runSession(session, tfvariables, variables, processId, tempDataHolder);
                    }
                }

                Object[] postProcessingList = (Object[]) config.get("postProcessing");
                if (postProcessingList != null && postProcessingList.length > 0) {
                    Map<String, TensorFlowPostProcessing> postsProcessingClasses = getPostProcessingClasses();
                    
                    for (Object postProcessingObj : postProcessingList) {
                        Map postProcessing = (Map) postProcessingObj;
                        String type = postProcessing.get("type").toString();
                        
                        postProcessing.put("processId", processId);
                        if (postsProcessingClasses.containsKey(type)) {
                            TensorFlowPostProcessing c = postsProcessingClasses.get(type);
                            c.runPostProcessing(postProcessing, tfvariables, variables, tempDataHolder);
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.error(TensorFlowUtil.class.getName(), e, "");
            }
        }
        
        return tfvariables;
    }
    
    protected static void runSession(Map session, Map<String, Object> tfvariables, Map<String, String> variables, String processId, Map<String, Object> tempDataHolder) {
        InputStream model = getInputStream(AppPluginUtil.getVariable(session.get("model").toString(), variables), null, null);
        try {
            Map<String, Tensor> inputMap = getInputs((Object[]) session.get("inputs"), processId, variables, tempDataHolder);
            Map<String, float[]> outputVariables = TensorFlowUtil.executeSimpleTensorFlowModel(model, inputMap, getOutputNames((Object[]) session.get("outputs")));
            outputToTfVariables((Object[]) session.get("outputs"), outputVariables, tfvariables);
        } catch (Exception e) {
            LogUtil.error(TensorFlowUtil.class.getName(), e, "");
        } finally {
            if (model != null) {
                try {
                    model.close();
                } catch (IOException ex) {}
            }
        }
    }
    
    protected static String[] getOutputNames(Object[] outputs) {
        Collection<String> names = new ArrayList<String>();
        
        if (outputs != null && outputs.length > 0) {
            for (Object outputObj : outputs) {
                Map output = (Map) outputObj;
                names.add(output.get("name").toString());
            }
        }
        
        return names.toArray(new String[0]);
    }
    
    protected static void outputToTfVariables(Object[] outputs, Map<String, float[]> outputVariables, Map<String, Object> tfvariables) {
        if (outputs != null && outputs.length > 0) {
            for (Object outputObj : outputs) {
                Map output = (Map) outputObj;
                tfvariables.put(output.get("variable").toString(), outputVariables.get(output.get("name").toString()));
            }
        }
    }
    
    protected static Map<String, Tensor> getInputs(Object[] inputs, String processId, Map<String, String> variables, Map<String, Object> tempDataHolder) throws IOException {
        Map<String, Tensor> inputMap = new HashMap<String, Tensor>();
        
        if (inputs != null && inputs.length > 0) {
            Map<String, TensorFlowInput> inputClasses = getInputClasses();
            
            for (Object inputObj : inputs) {
                Map input = (Map) inputObj;
                String type = input.get("type").toString();
                String name = input.get("name").toString();
                        
                if (inputClasses.containsKey(type)) {
                    TensorFlowInput c = inputClasses.get(type);
                    inputMap.put(name, c.getInputs(input, processId, variables, tempDataHolder));
                }
            }
        }
        
        return inputMap;
    }
    
    protected static Map<String, TensorFlowInput> getInputClasses() {
        Map<String, TensorFlowInput> inputs = new TreeMap<String, TensorFlowInput>();
        
        if (defaultInputClasses == null) {
            defaultInputClasses = new HashMap<String, TensorFlowInput>();
            ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
            provider.addIncludeFilter(new AssignableTypeFilter(TensorFlowInput.class));
            Set<BeanDefinition> components = provider.findCandidateComponents("org.joget");
            for (BeanDefinition component : components) {
                String beanClassName = component.getBeanClassName();
                try {
                    Class<? extends TensorFlowInput> beanClass = Class.forName(beanClassName).asSubclass(TensorFlowInput.class);
                    TensorFlowInput i = beanClass.newInstance();
                    defaultInputClasses.put(i.getName(), i);
                } catch (Exception ex) {}
            }
        }
        inputs.putAll(defaultInputClasses);
        
        //retrieve from osgi plugins
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        Collection<Plugin> plugins = pluginManager.listOsgiPlugin(TensorFlowPlugin.class);
        for (Plugin p : plugins) {
            TensorFlowPlugin tfp = (TensorFlowPlugin) p;
            TensorFlowInput[] temp = tfp.getInputClasses();
            if (temp != null && temp.length > 0) {
                for (TensorFlowInput i : temp) {
                    inputs.put(i.getName(), i);
                }
            }
        }
        
        return inputs;
    }
    
    protected static Map<String, TensorFlowPostProcessing> getPostProcessingClasses() {
        Map<String, TensorFlowPostProcessing> posts = new TreeMap<String, TensorFlowPostProcessing>();
     
        if (defaultPostClasses == null) {
            defaultPostClasses = new HashMap<String, TensorFlowPostProcessing>();
            ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
            provider.addIncludeFilter(new AssignableTypeFilter(TensorFlowPostProcessing.class));
            Set<BeanDefinition> components = provider.findCandidateComponents("org.joget");
            for (BeanDefinition component : components) {
                String beanClassName = component.getBeanClassName();
                try {
                    Class<? extends TensorFlowPostProcessing> beanClass = Class.forName(beanClassName).asSubclass(TensorFlowPostProcessing.class);
                    TensorFlowPostProcessing i = beanClass.newInstance();
                    defaultPostClasses.put(i.getName(), i);
                } catch (Exception ex) {}
            }
        }
        posts.putAll(defaultPostClasses);
        
        //retrieve from osgi plugins
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        Collection<Plugin> plugins = pluginManager.listOsgiPlugin(TensorFlowPlugin.class);
        for (Plugin p : plugins) {
            TensorFlowPlugin tfp = (TensorFlowPlugin) p;
            TensorFlowPostProcessing[] temp = tfp.getPostProcessingClasses();
            if (temp != null && temp.length > 0) {
                for (TensorFlowPostProcessing i : temp) {
                    posts.put(i.getName(), i);
                }
            }
        }
        
        return posts;
    }
    
    public static void convertTFVariables(Map<String, Object> tfvariables, Map<String, String> variables) {
        for (String name : tfvariables.keySet()) {
            if (tfvariables.get(name) instanceof String || tfvariables.get(name) instanceof Float) {
                variables.put(name, tfvariables.get(name).toString());
            } else if (tfvariables.get(name) instanceof float[]) {
                String value = "";
                DecimalFormat df = new DecimalFormat("0.00");
                for (Float f : (float[]) tfvariables.get(name)) {
                    if (!value.isEmpty()) {
                        value += ";";
                    }
                    value += df.format(f);
                }
                variables.put(name, value);
            }
        }
        TensorFlowUtil.debug("Variables : ", variables);
    }
    
    public static void debug(String message, Object obj) {
        if (LogUtil.isDebugEnabled(TensorFlowUtil.class.getName())) {
            if (obj != null) {
                message += " : " + TensorFlowUtil.outputToJson(obj);
            }
            LogUtil.debug(TensorFlowUtil.class.getName(), message);
        }
    }
}
