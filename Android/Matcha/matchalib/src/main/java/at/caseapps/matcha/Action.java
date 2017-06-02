package at.caseapps.matcha;

import java.util.Map;

public class Action {

    public String name;
    public String element;
    public Map<String, Object> parameters;
    public int line = 0;

    public double wait = 2.0;
    public double pollInterval = 0.25;

    public Action(String name, String firstParameter, Map<String, Object> parameters, int line) {
        this.name = name;
        this.element = firstParameter;
        if (parameters != null && parameters.isEmpty() == false) {
            this.parameters = parameters;
        }
        this.line = line;
    }

    public static Action createAction(String name, String firstParameter, Map<String, Object> parameters, int line) {
        if (name.equals("scroll")) {
            return new ScrollAction(name, firstParameter, parameters, line);
        }
        if (name.equals("enter")) {
            return new EnterAction(name, firstParameter, parameters, line);
        }
        if (name.equals("verify")) {
            if (parameters.get("navigationTitle") != null) {
                return new VerifyNavigationAction(name, firstParameter, parameters, line);
            } else {
                return new VerifyAction(name, firstParameter, parameters, line);
            }
        }
        if (name.equals("contains")) {
            return new VerifyAction(name, firstParameter, parameters, line);
        }
        if (name.equals("executeJS")) {
            return new ExecuteJSAction(name, firstParameter, parameters, line);
        }
        if (name.equals("http")) {
            return new HttpAction(name, firstParameter, parameters, line);
        }
        if (name.equals("wait")) {
            return new WaitAction(name, firstParameter, parameters, line);
        }
        if (name.equals("searchField")) {
            return new SearchFieldAction(name, firstParameter, parameters, line);
        }
        return new Action(name, firstParameter, parameters, line);
    }

    public static class HttpAction extends Action {
        public String url;
        public String header;
        public String params;

        public HttpAction(String name, String firstParameter, Map<String, Object> parameters, int line) {
            super(name, firstParameter, parameters, line);
            this.url = (String) parameters.get("url");
            this.header = (String) parameters.get("headers");
            this.params = (String) parameters.get("params");
        }
    }

    public static class ScrollAction extends Action {
        public String to;
        public String direction = "right";
        public int amount = 500;

        public ScrollAction(String name, String firstParameter, Map<String, Object> parameters, int line) {
            super(name, firstParameter, parameters, line);
            this.to = (String) parameters.get("to");
            if (parameters.get("direction") != null) {
                this.direction = (String) parameters.get("direction");
            }
            if (parameters.get("amount") != null) {
                this.amount = Integer.valueOf((String) parameters.get("amount"));
            }
        }
    }

    public static class ExecuteJSAction extends Action {
        public String code;

        public ExecuteJSAction(String name, String firstParameter, Map<String, Object> parameters, int line) {
            super(name, firstParameter, parameters, line);
            if (parameters.get("value") != null) {
                this.code = (String) parameters.get("value");
            } else {
                this.code = (String) parameters.get("code");
            }

        }
    }

    public static class VerifyNavigationAction extends Action {
        public String navigationTitle;

        public VerifyNavigationAction(String name, String firstParameter, Map<String, Object> parameters, int line) {
            super(name, firstParameter, parameters, line);
            if (parameters.get("value") != null) {
                this.navigationTitle = (String) parameters.get("value");
            } else {
                this.navigationTitle = (String) parameters.get("navigationTitle");
            }
        }
    }

    public static class VerifyAction extends Action {
        public String value;

        public VerifyAction(String name, String firstParameter, Map<String, Object> parameters, int line) {
            super(name, firstParameter, parameters, line);
            this.value = (String) parameters.get("value");
        }
    }

    public static class EnterAction extends Action {
        public String value;

        public EnterAction(String name, String firstParameter, Map<String, Object> parameters, int line) {
            super(name, firstParameter, parameters, line);
            this.value = (String) parameters.get("value");
        }
    }

    public static class WaitAction extends Action {
        public double value = 0.5;

        public WaitAction(String name, String firstParameter, Map<String, Object> parameters, int line) {
            super(name, firstParameter, parameters, line);
            if (parameters.get("value") != null) {
                this.value = (double) parameters.get("value");
            } else if (firstParameter != null) {
                this.value = Double.valueOf(firstParameter);
            }
        }
    }

    public static class SearchFieldAction extends Action {
        public String value = "";

        public SearchFieldAction(String name, String firstParameter, Map<String, Object> parameters, int line) {
            super(name, firstParameter, parameters, line);
            if (parameters.get("value") != null) {
                this.value = (String) parameters.get("value");
            } else if (firstParameter != null) {
                this.value = firstParameter;
            }
        }
    }
}
    


