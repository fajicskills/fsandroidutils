package com.fajicskills.fsandroidutils.bus.events;


public class SearchPerformedEvent {

    // region Member Variables
    private String query;
    // endregion

    // region Constructors
    public SearchPerformedEvent(String query){
        this.query = query;
    }
    // endregion

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
