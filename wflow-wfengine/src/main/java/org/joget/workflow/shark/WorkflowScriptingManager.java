package org.joget.workflow.shark;

import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.scripting.Evaluator;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.enhydra.shark.scripting.BshEvaluator;
import org.enhydra.shark.scripting.PythonEvaluator;
import org.enhydra.shark.scripting.StandardScriptingManager;

public class WorkflowScriptingManager extends StandardScriptingManager {

    public static final String PYTHON_SCRIPT = "text/pythonscript";
    public static final String JAVA_LANGUAGE_SCRIPT = "text/java";
    public static final String JAVA_SCRIPT = "text/javascript";
    private BshEvaluator bshEvaluator;
    private PythonEvaluator pythonEvaluator;
    private WorkflowJavaScriptEvaluator jsEvaluator;
    private CallbackUtilities cus;

    public void configure(CallbackUtilities cus) throws Exception {
        this.cus = cus;
        bshEvaluator = new BshEvaluator();
        bshEvaluator.configure(cus);
        pythonEvaluator = new PythonEvaluator();
        pythonEvaluator.configure(cus);
        jsEvaluator = new WorkflowJavaScriptEvaluator();
        jsEvaluator.configure(cus);
    }

    public Evaluator getEvaluator(WMSessionHandle shandle, String name) throws Exception {
        if (name == null) {
            return null;
        }
        Evaluator eval = null;
        if (name.equals(PYTHON_SCRIPT)) {
            eval = pythonEvaluator;
        } else if (name.equals(JAVA_LANGUAGE_SCRIPT)) {
            eval = bshEvaluator;
        } else if (name.equals(JAVA_SCRIPT)) {
            eval = jsEvaluator;
        }
        return eval;
    }
}
