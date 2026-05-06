package com.example.transaction_service.config;

import com.example.transaction_service.activity.AccountActivitiesImpl;
import com.example.transaction_service.workflow.FundTransferWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TemporalConfig {
    public static final String TRANSACTION_TASK_QUEUE="TRANSACTION_TASK_QUEUE";
    @Bean
    public WorkflowServiceStubs workflowServiceStubs(){
        return WorkflowServiceStubs.newLocalServiceStubs();
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs workflowServiceStubs){
        return WorkflowClient.newInstance(workflowServiceStubs);
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient, AccountActivitiesImpl accountActivities){
        WorkerFactory factory=WorkerFactory.newInstance(workflowClient);
        // 1. Create a Worker listening to our specific Task Queue
        Worker worker = factory.newWorker(TRANSACTION_TASK_QUEUE);

        // 2. Register the Workflow Implementation (Pass the Class)
        worker.registerWorkflowImplementationTypes(FundTransferWorkflowImpl.class);

        // 3. Register the Activity Implementation (Pass the Spring Bean Instance)
        worker.registerActivitiesImplementations(accountActivities);

        // 4. Start the factory to begin polling for tasks
        factory.start();
        return factory;

    }
}
