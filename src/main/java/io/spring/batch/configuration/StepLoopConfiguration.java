package io.spring.batch.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableBatchProcessing
public class StepLoopConfiguration {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean(name="JOB4")
    public Job executeMyJob() {
        List<Step> steps = new ArrayList<Step>();
/*        String[] strs = {"1", "2", "3",};
        List<String> dates = Arrays.asList(strs);
        for (String date : dates) {
            steps.add(createStep(date));
        }
        */
//step loop
        List<Integer> stepTest = new ArrayList<>();
        for(int i=0; i< 100; i++ ){
            stepTest.add(i);
        }
        for (Integer date : stepTest) {
            steps.add(createStep(date));
        }

        return jobBuilderFactory.get("executeMyJob11")
                .start(createParallelFlow(steps))
                .end()
                .build();
    }

    private Step createStep(Integer date){
        return stepBuilderFactory.get("test_stop_001 : " + date)
                .tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Hello World!");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    private Flow createParallelFlow(List<Step> steps) {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        // max multithreading = -1, no multithreading = 1, smart size = steps.size()
        taskExecutor.setConcurrencyLimit(1);

        List<Flow> flows = steps.stream()
                .map(step -> new FlowBuilder<Flow>("flow_" + step.getName()).start(step).build())
                .collect(Collectors.toList());

        return new FlowBuilder<SimpleFlow>("parallelStepsFlow")
                .split(taskExecutor)
                .add(flows.toArray(new Flow[flows.size()]))
                .build();
    }
}
