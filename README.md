# Fintech Q&A


## PGVectorStore

To run the PgVecgtorStore locally, go to the `src/main/resources/docker` directory and run:

```
docker-compose up
```

Later starts Postgres DB on localhost and port 5432.

Then you can connect to the database (password: `postgres`) and inspect or alter the `vector_store` table content:

```
psql -U postgres -h localhost -p 5432

\l
\c postgres
\dt

select count(*) from vector_store;

delete from vector_store;
```

Here is an example output for Uber and Lyft.

```
> Q: What is the revenue of {company} in 2022? Answer in millions, with page reference
  A: The revenue of Uber in 2022 was $31,877 million. (Page reference: Comparison of the Years Ended December 31, 2021 and 2022)
> Q: What is the revenue of {company} in 2022? Answer in millions, with page reference
  A: The revenue of Lyft in 2022 is $4,095,135 million. (Page reference: 1)
> Q: What customer segments grew the fastest for {company} in 2022?
  A: The annual 10-K report does not provide specific information on the fastest-growing customer segments for Uber in 2022. It focuses more on overall market dynamics, demand fluctuations, and the impact of the COVID-19 pandemic.
> Q: What customer segments grew the fastest for {company} in 2022?
  A: The document does not provide information about the specific customer segments that grew the fastest for Lyft in 2022.
> Q: What geographies grew the fastest for {company} in 2022?
  A: The document does not provide specific information about the geographies that grew the fastest for Uber in 2022.
> Q: What geographies grew the fastest for {company} in 2022?
  A: The 10-K report does not provide specific information on the geographies that grew the fastest for Lyft in 2022.
> Q: How {company} performed in the different geographies in 2022?
  A: The provided information does not contain specific details about how Uber performed in different geographies in 2022. Therefore, I don't have the necessary data to answer your question accurately.
> Q: How {company} performed in the different geographies in 2022?
  A: The provided information does not specify how Lyft performed in different geographies in 2022.
> Q: What was the COVID-19 impact on {company}'s business between 2021 and 2022?
  A: The information provided in the documents does not specifically mention the impact of COVID-19 on Uber's business between 2021 and 2022. It states that in 2021, there was some recovery from the onset of the pandemic as vaccines were widely distributed and communities reopened, resulting in improved performance compared to 2020. It also mentions sequential quarterly improvements in demand and overall marketplace health in 2022, with the fourth quarter of 2022 having the highest number of Active Riders in nearly three years. However, it does not provide specific details about the COVID-19 impact during that period.
> Q: What was the COVID-19 impact on {company}'s business between 2021 and 2022?
  A: According to the information provided, in 2021, Lyft saw some recovery from the onset of the COVID-19 pandemic as vaccines were more widely distributed and more communities fully reopened, resulting in improved performance compared to 2020. In 2022, while there was a decrease in demand in the first quarter due to an increase in cases due to variants of the virus, there were sequential quarterly improvements in demand and overall marketplace health. In the fourth quarter of 2022, Lyft had the highest numbers of Active Riders in nearly three years, and for the full year 2022, Lyft generated revenue of $4.1 billion, the highest since its inception. However, the costs also increased, and in the fourth quarter of 2022, Lyft strengthened its insurance reserves and accrued other current liabilities by $375 million to mitigate exposure to fluctuations. Overall, it appears that the COVID-19 pandemic had both positive and negative impacts on Lyft's business between 2021 and 2022.
> Q: What is the revenue growth of {company} from 2021 to 2022?
  A: The revenue growth of Uber from 2021 to 2022 was 83%.
> Q: What is the revenue growth of {company} from 2021 to 2022?
  A: The revenue growth of Lyft from 2021 to 2022 is 83%. The revenue increased from $17,455 million in 2021 to $31,877 million in 2022.
```