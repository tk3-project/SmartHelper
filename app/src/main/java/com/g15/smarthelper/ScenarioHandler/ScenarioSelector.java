package com.g15.smarthelper.ScenarioHandler;

import com.g15.smarthelper.Scenarios;

public class ScenarioSelector {
    private ScenarioSelector() {}

    public static ScenarioHandler getScenarioHandler(Scenarios.Scenario scenario) {
        switch (scenario) {
            case SCENARIO_HOME: return new ScenarioHome();
            case SCENARIO_WARNING: return new ScenarioWarning();
            case SCENARIO_MUSIC: return new ScenarioMusic();
            default: return null;
        }
    }
}
