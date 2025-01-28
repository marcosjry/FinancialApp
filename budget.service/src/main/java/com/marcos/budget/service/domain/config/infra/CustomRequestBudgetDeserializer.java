package com.marcos.budget.service.domain.config.infra;

import com.google.gson.*;
import com.marcos.budget.service.domain.DTO.request.RequestBudget;
import com.marcos.budget.service.domain.DTO.request.RequestCreateBudgetDTO;
import com.marcos.budget.service.domain.DTO.request.RequestCreateTransactionBudget;
import com.marcos.budget.service.domain.DTO.request.RequestDeleteBudget;

import java.lang.reflect.Type;

public class CustomRequestBudgetDeserializer implements JsonDeserializer<RequestBudget> {

    @Override
    public RequestBudget deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String action = jsonObject.get("action").getAsString();

        // Criar a instância da classe base
        RequestBudget requestBudget = new RequestBudget();
        requestBudget.setAction(action);

        // Desserializar o campo 'data' dependendo da ação
        JsonElement dataElement = jsonObject.get("data");
        if ("create_budget".equals(action)) {
            RequestCreateBudgetDTO data = context.deserialize(dataElement, RequestCreateBudgetDTO.class);
            requestBudget.setData(data);
        } else if ("delete_budget".equals(action)) {
            RequestDeleteBudget data = context.deserialize(dataElement, RequestDeleteBudget.class);
            requestBudget.setData(data);
        } else if ("create_budget_transaction".equals(action)) {
            RequestCreateTransactionBudget data = context.deserialize(dataElement, RequestCreateTransactionBudget.class);
            requestBudget.setData(data);
        }

        return requestBudget;
    }
}
