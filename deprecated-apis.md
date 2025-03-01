# Deprecated APIs Found

## 1. DataGeneratorSource (RichSourceFunction)
**Location**: flink-data-generator/src/main/java/io/confluent/developer/generator/DataGeneratorSource.java
**Issue**: Uses deprecated RichSourceFunction API
**Recommendation**: Migrate to the new Source API introduced in Flink 1.12
**Migration Steps**:
1. Implement Source interface instead of RichSourceFunction
2. Use Source.from() for better integration with Flink's new source architecture
3. Implement StatefulSource if checkpointing is needed

Example structure:
```java
public class DataGeneratorSource implements Source<Flight, SourceSplit, EnumeratorState> {
    // Implementation details...
}
```

## 2. Version Inconsistency
**Location**: 
- build.gradle.kts
- flink-data-generator/build.gradle.kts
**Issue**: Kafka connector uses explicit version (3.4.0-1.20) while other Flink components use flinkVersion (1.20.0)
**Recommendation**: Align all Flink component versions
**Fix**:
```kotlin
implementation("org.apache.flink:flink-connector-kafka:$flinkVersion")
```

## Impact Analysis
1. DataGeneratorSource migration:
   - Requires code changes
   - Affects data generation pipeline
   - Benefits: Better integration with Flink's modern architecture
2. Version alignment:
   - Simple configuration change
   - Ensures consistent behavior across Flink components
   - Reduces potential compatibility issues

## Migration Priority
1. High: Version alignment (simple fix with immediate benefit)
2. Medium: DataGeneratorSource migration (requires careful planning and testing)