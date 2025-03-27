package com.bept4.ticketplatform.model;

public enum Status {
    TO_DO,
    IN_PROGRESS,
    COMPLETED;

    public String getLabel() {
        return switch (this) {
            case TO_DO -> "To Do";
            case IN_PROGRESS -> "In Progress";
            case COMPLETED -> "Completed";
        };
    }

    public String getBadgeClass() {
        return switch (this) {
            case TO_DO -> "text-bg-warning";
            case IN_PROGRESS -> "text-bg-info";
            case COMPLETED -> "text-bg-success";
        };
    }

}
