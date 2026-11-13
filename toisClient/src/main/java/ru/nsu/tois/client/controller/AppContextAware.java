package ru.nsu.tois.client.controller;

import ru.nsu.tois.client.session.AppContext;

public interface AppContextAware {
    void setAppContext(AppContext appContext);
}
