package org.joget.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.Buffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.io.IOUtils;
import org.joget.commons.util.LogUtil;
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
    
    public static Tensor imageInput(InputStream inputStream, String imageType, int height, int width, float mean, float scale, String type) throws IOException {
        DataType dataType = getDataType(type);
        byte[] imageBytes = IOUtils.toByteArray(inputStream);
        try (Graph g = new Graph()) {
            GraphBuilder b = new GraphBuilder(g);
            final Output input = b.constant("input", imageBytes);
            LogUtil.debug(TensorFlowUtil.class.getName(), "Normalizing image " + imageType);
            Output imageOutput = b.decodeJpeg(input, 3);
            LogUtil.debug(TensorFlowUtil.class.getName(), "Decoded image " + imageType);
            final Output output
                    = b.cast(
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
            try (Session s = new Session(g)) {
                Tensor result = s.runner().fetch(output.op().name()).run().get(0);
                LogUtil.debug(TensorFlowUtil.class.getName(), "Normalized image " + imageType);
                return result;
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
                float[] result;
                long[] rshape = tensor.shape();
                long resultSize = (rshape.length == 1) ? 1 : rshape[1];
                if (resultSize == 1) {
                    if (tensor.dataType().equals(DataType.INT32) || tensor.dataType().equals(DataType.INT64) || tensor.dataType().equals(DataType.UINT8)) {
                        int[] tempResult = new int[1];
                        tensor.copyTo(tempResult);
                        result = new float[1];
                        result[0] = tempResult[0];
                    } else {
                        float[] tempResult = new float[1];
                        tensor.copyTo(tempResult);
                        result = tempResult;
                    }
                } else {
                    float[][] tempResult = new float[1][(int)resultSize];
                    tensor.copyTo(tempResult);
                    result = tempResult[0];
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
        return resultMap;
    }
    
    public static List<String> getValueToLabelList(InputStream labelInputStream, float[] results, Integer numberOfValues, Boolean unique) throws IOException {
        List<String> labels = IOUtils.readLines(labelInputStream);
        List<String> resultLabels = new ArrayList<String>();
        if (numberOfValues == null) {
            numberOfValues = results.length;
        }
        for (int i=0; i < numberOfValues; i++) {
            String label = labels.get((int)(results[i] - 1));
            if (unique == null || !unique || (unique && !resultLabels.contains(label))) {
                resultLabels.add(label);
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
}
