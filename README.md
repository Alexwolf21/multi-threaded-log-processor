# Multi-Threaded Log Processor

A Java CLI application that processes server log files concurrently using a producer-consumer architecture and outputs aggregated metrics as JSON.

## Features

- Multiple **producer threads** read files concurrently.
- Multiple **consumer threads** parse and aggregate log lines concurrently.
- Shared **bounded blocking queue** between producers and consumers.
- Malformed log lines are skipped and counted.
- Per-service metrics:
  - total requests
  - status code counts
  - average latency
  - p95 latency
- Centralized logging:
  - `INFO` for lifecycle
  - `WARN` for malformed lines
  - `ERROR` (`SEVERE`) for failures

## Log Format

Each line must follow:

`timestamp service method path status latency`

Example:

`2026-02-19T12:34:56.789Z service-A GET /api/v1/users 200 123ms`

## Build

```bash
./scripts/build.sh
```

Produces:

`<repo-root>/logproc.jar`

## Run

### Recommended (works from any current directory)

```bash
./scripts/run.sh \
  --input logs/ \
  --producers 2 \
  --consumers 4 \
  --queue-size 1000 \
  --output summary.json
```

### Direct java command

If you use `java -jar`, run it from repo root or use an absolute jar path:

```bash
java -jar /absolute/path/to/repo/logproc.jar \
  --input /absolute/path/to/logs \
  --producers 2 \
  --consumers 4 \
  --queue-size 1000 \
  --output /absolute/path/to/summary.json
```

## Output

JSON summary fields:

- `totalFiles`
- `totalLines`
- `malformedLines`
- `processingTimeMs`
- `services` map with:
  - `totalRequests`
  - `statusCounts`
  - `avgLatencyMs`
  - `p95LatencyMs`

## Tests

Run all tests:

```bash
./scripts/test.sh
```

Test coverage includes:

- Unit test for parser validity/malformed detection
- Unit test for service aggregation + p95
- Integration test for end-to-end CLI execution and summary generation
