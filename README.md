# Spring PetClinic: A DDD & Modulith Reference Implementation

This project is a modernized version of the classic Spring PetClinic application, serving as a **practical reference** for implementing **Domain-Driven Design (DDD)** with **Spring Modulith** and **jMolecules**.

Use this as a guide when starting new projects or as a refresher on tactical DDD patterns.

---

## Why Domain-Driven Design?

### The Problem DDD Solves

As software systems grow, they tend to become **big balls of mud**—tangled codebases where business logic is scattered across controllers, services, and utilities. Changes become risky. New developers take months to become productive. The code no longer reflects how the business actually works.

DDD addresses this by:

1. **Aligning code with business reality** — The domain model mirrors how domain experts think and talk about the problem. When the business says "a pet is adopted by an owner," that's exactly what the code expresses.

2. **Managing complexity through boundaries** — Large systems are decomposed into **Bounded Contexts**, each with its own model and language. The "Customer" in billing is not the same as the "Customer" in shipping—and the code acknowledges this.

3. **Protecting business logic** — Domain rules live in the domain layer, not scattered across controllers and services. The model enforces invariants, making it impossible to create invalid states.

### Why DDD Is Popular Now

DDD was published in 2003 but has seen a renaissance in recent years:

- **Microservices need boundaries** — Teams discovered that decomposing monoliths without clear domain boundaries leads to distributed monoliths. DDD's Bounded Contexts provide a principled way to define service boundaries.

- **Event-driven architecture** — Modern systems communicate through events. DDD's Domain Events pattern maps directly to event sourcing and message-driven microservices.

- **Complexity is increasing** — As businesses digitize, software must model increasingly complex domains. CRUD-style thinking doesn't scale.

- **Better tooling** — Frameworks like Spring Modulith and jMolecules finally make DDD practical in Java/Spring without fighting the framework.

### DDD and Microservices: A Natural Fit

```
                         Monolith with Modules
    ┌─────────────────────────────────────────────────────────┐
    │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
    │  │   Owner     │  │     Vet     │  │   Billing   │     │
    │  │  Context    │──│   Context   │──│   Context   │     │
    │  │  (Module)   │  │  (Module)   │  │  (Module)   │     │
    │  └─────────────┘  └─────────────┘  └─────────────┘     │
    │         │                │                │             │
    │         └────── Events ──┴──── Events ────┘             │
    └─────────────────────────────────────────────────────────┘
                                │
                                │ Extract when ready
                                ▼
                        Microservices
    ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
    │   Owner     │     │     Vet     │     │   Billing   │
    │  Service    │────▶│   Service   │────▶│   Service   │
    │             │     │             │     │             │
    └─────────────┘     └─────────────┘     └─────────────┘
          │                   │                   │
          └────── Kafka/RabbitMQ ─────────────────┘
```

**The mapping:**

| DDD Concept | Monolith | Microservices |
|-------------|----------|---------------|
| Bounded Context | Spring Modulith Module | Separate Service |
| Aggregate | Transactional boundary | Service boundary |
| Domain Event | `ApplicationEventPublisher` | Kafka/RabbitMQ message |
| Anti-Corruption Layer | Module adapter | API Gateway / BFF |

**The strategy:** Start with a **modular monolith**. Define boundaries with Spring Modulith. Communicate through events. When a module needs independent scaling or deployment, extract it—the boundaries are already clean.

### When NOT to Use DDD

DDD is not free. It adds concepts, abstractions, and ceremony. **Don't use DDD when:**

| Scenario | Why DDD Is Overkill |
|----------|---------------------|
| **Simple CRUD apps** | If your app is mostly forms over data with little business logic, DDD adds complexity without benefit. A simple layered architecture suffices. |
| **Short-lived projects** | Prototypes, MVPs, or throwaway code don't benefit from the upfront investment DDD requires. |
| **Small teams without domain experts** | DDD assumes collaboration with domain experts. Without them, you're just guessing at the model. |
| **Well-understood, stable domains** | If the domain is simple and unlikely to change, the flexibility DDD provides isn't needed. |

### The Trade-offs

**Benefits:**
- Code reflects business language (Ubiquitous Language)
- Clear boundaries make the system easier to reason about
- Changes are localized to specific modules/contexts
- Natural path to microservices when needed
- Domain logic is testable without infrastructure

**Costs:**
- **Learning curve** — Aggregates, Bounded Contexts, Value Objects, Domain Events... many concepts to internalize
- **Upfront investment** — Modeling the domain takes time before coding starts
- **More code** — Value Objects, DTOs, event classes add lines of code
- **Risk of over-engineering** — Applied dogmatically, DDD can make simple things complicated
- **Requires discipline** — The team must consistently respect boundaries and patterns

**The pragmatic approach:** Use DDD **tactically** (the patterns in this guide) for complex domain logic. Use DDD **strategically** (Bounded Contexts, Context Maps) when you have multiple teams or are planning microservices. Don't apply it everywhere—use it where complexity justifies it.

---

## About This Project

Back in 2003, Eric Evans published "Domain-Driven Design: Tackling Complexity in the Heart of Software" (the Blue Book). For years, applying DDD to Spring/Hibernate applications meant wrestling with anemic domain models—entities reduced to mere data containers with getters and setters, while business logic scattered across service layers.

Thanks to [Oliver Drotbohm](https://odrotbohm.de/) and the work on Spring Modulith and jMolecules, we no longer need to compromise. This project demonstrates how to build **rich domain models** with proper encapsulation, enforce **module boundaries** at compile time, and prepare a monolith for eventual **microservice extraction**—all while keeping the pragmatism that makes Spring productive.

---

## Tactical DDD Building Blocks

### 1. Type-Safe Identifiers

**Problem:** Primitive obsession—using `Integer` or `Long` for IDs leads to accidental mixing of different entity IDs.

**Solution:** Wrap identifiers in type-safe records implementing `Identifier`.

```java
public record PetId(@Column(name = "id") UUID value) implements Identifier {
    public PetId() { this(UUID.randomUUID()); }
}

public record OwnerId(@Column(name = "id") UUID value) implements Identifier {
    public OwnerId() { this(UUID.randomUUID()); }
}
```

**Benefits:**
- Compile-time safety: can't pass `OwnerId` where `PetId` is expected
- UUIDs work better for distributed systems than auto-increment
- Self-documenting code

**Usage in entities:**
```java
public class Pet implements Entity<Owner, PetId> {
    private PetId id = new PetId();

    public PetId getId() { return this.id; }
}
```

---

### 2. Value Objects

**Problem:** Primitive fields with behavior scattered across services (anemic model).

**Solution:** Create immutable Value Objects that encapsulate both data and behavior.

```java
public record BirthDate(@Column(name = "birth_date") LocalDate date) implements ValueObject {

    public BirthDate {
        if (date == null) throw new IllegalArgumentException("Birth date must not be null");
        if (date.isAfter(LocalDate.now())) throw new IllegalArgumentException("Birth date cannot be in the future");
    }

    public int getAgeInYears() {
        return Period.between(date, LocalDate.now()).getYears();
    }

    public boolean isElderly() { return getAgeInYears() >= 7; }

    public boolean isPuppy() { return getAgeInYears() < 1; }

    public String getAgeDescription() {
        int years = getAgeInYears();
        return years == 1 ? "1 year" : years + " years";
    }
}
```

**Other examples in this project:**
- `Address` — street, city with composite formatting
- `Telephone` — phone number validation
- `PersonName` — first/last name handling
- `VisitDescription` — visit notes with constraints

**Key characteristics:**
- Immutable (use records)
- Self-validating (validate in constructor)
- Behavior lives with the data
- Equality by value, not identity

---

### 3. Entities & Aggregates

**Entity:** Has identity that persists across state changes. Two entities with the same data but different IDs are different.

**Aggregate:** Cluster of entities treated as a single unit. One entity is the **Aggregate Root**—all access goes through it.

```java
// Owner is the Aggregate Root
public class Owner extends Person implements AggregateRoot<Owner, OwnerId> {
    private OwnerId id = new OwnerId();
    private Set<Pet> pets = new LinkedHashSet<>();  // Pets belong to this aggregate

    public void addPet(Pet pet) {
        pets.add(pet);
        // Publish domain event
    }

    public Pet getPet(String name) {
        return pets.stream()
            .filter(p -> p.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
}

// Pet is an Entity within the Owner aggregate
public class Pet extends NamedEntity implements Entity<Owner, PetId> {
    private PetId id = new PetId();
    private BirthDate birthDateValue;
    private Set<Visit> visits = new LinkedHashSet<>();

    public void addVisit(Visit visit) {
        visits.add(visit);
    }
}
```

**Rules:**
- Only the Aggregate Root has a repository
- External objects reference the aggregate by ID only
- Invariants are enforced within the aggregate boundary

---

### 4. Associations (Cross-Aggregate References)

**Problem:** Direct references between aggregates create tight coupling and violate boundaries.

**Solution:** Use `Association<T, ID>` to hold only the ID reference, not the full entity.

```java
public class Pet implements Entity<Owner, PetId> {
    // Don't do this - crosses aggregate boundary
    // private PetType type;

    // Do this - store only the reference
    private Association<PetType, PetTypeId> type;

    public void setType(PetType type) {
        this.type = type != null ? Association.forAggregate(type) : null;
    }

    public PetTypeId getTypeId() {
        return this.type != null ? this.type.getId() : null;
    }

    // Resolve when needed (typically in application layer)
    public PetType resolveType(AssociationResolver<PetType, PetTypeId> resolver) {
        return this.type != null ? resolver.resolve(this.type).orElse(null) : null;
    }
}
```

**When to use Associations:**
- Reference data (PetType, Specialty, Category)
- Cross-module references
- Any reference that would create a large object graph

---

### 5. Domain Events

**Problem:** Modules need to react to changes in other modules without direct coupling.

**Solution:** Publish domain events that other modules can subscribe to.

**Defining an event:**
```java
public record PetAdoptedEvent(
    PetId petId,
    PetTypeId petTypeId,
    OwnerId ownerId,
    LocalDate adoptionDate
) implements DomainEvent {

    public static PetAdoptedEvent of(PetId petId, PetTypeId petTypeId, OwnerId ownerId) {
        return new PetAdoptedEvent(petId, petTypeId, ownerId, LocalDate.now());
    }
}
```

**Publishing (from aggregate or application service):**
```java
@Service
public class PetApplicationService {
    private final ApplicationEventPublisher events;

    public void adoptPet(Owner owner, Pet pet) {
        owner.addPet(pet);
        owners.save(owner);
        events.publishEvent(PetAdoptedEvent.of(pet.getId(), pet.getTypeId(), owner.getId()));
    }
}
```

**Subscribing (in another module):**
```java
@Service
class VetPatientTrackingService {

    @ApplicationModuleListener
    void onPetAdopted(PetAdoptedEvent event) {
        log.info("New patient registered: Pet ID=" + event.petId());
        // Update statistics, notify vets, etc.
    }
}
```

---

### 6. Layered Validation

**Problem:** Bean Validation annotations (`@NotBlank`, `@Email`) pollute domain entities with framework concerns.

**Solution:** Three-layer validation strategy.

**Web Layer — DTOs with Bean Validation:**
```java
public record PetFormData(
    @NotBlank(message = "Pet name is required")
    String name,

    @NotNull(message = "Pet type is required")
    String typeName,

    @NotNull(message = "Birth date is required")
    @PastOrPresent(message = "Birth date cannot be in the future")
    LocalDate birthDate
) {
    public Pet toDomainObject(PetType type) {
        Pet pet = new Pet();
        pet.setName(this.name);
        pet.setType(type);
        pet.setBirthDate(this.birthDate);
        return pet;
    }
}
```

**Application Layer — Business Rules:**
```java
@PostMapping("/pets/new")
public String processCreationForm(Owner owner, @Valid PetFormData formData, BindingResult result) {
    // Business rule: no duplicate pet names for same owner
    if (owner.getPet(formData.name(), true) != null) {
        result.rejectValue("name", "duplicate", "A pet with this name already exists");
    }

    if (result.hasErrors()) {
        return "pets/createOrUpdatePetForm";
    }

    Pet pet = formData.toDomainObject(findType(formData.typeName()));
    owner.addPet(pet);
    owners.save(owner);
    return "redirect:/owners/" + owner.getId();
}
```

**Domain Layer — Invariants in Value Objects:**
```java
public record BirthDate(LocalDate date) implements ValueObject {
    public BirthDate {
        if (date == null) throw new IllegalArgumentException("Birth date must not be null");
        if (date.isAfter(LocalDate.now())) throw new IllegalArgumentException("Cannot be in the future");
    }
}
```

---

## Spring Modulith Architecture

### Module Structure

```
┌─────────────────────────────────────────────────────────────┐
│                    Spring PetClinic                          │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────────┐         ┌──────────────────┐          │
│  │  Owner Module    │         │   Vet Module     │          │
│  │                  │ events  │                  │          │
│  │  - Owner         │────────▶│  - Vet           │          │
│  │  - Pet           │         │  - Specialty     │          │
│  │  - Visit         │         │  - Patient       │          │
│  │  - PetType       │         │    Tracking      │          │
│  └────────┬─────────┘         └────────┬─────────┘          │
│           │                            │                     │
│           ▼                            ▼                     │
│  ┌─────────────────────────────────────────────────┐        │
│  │              Shared Kernel (model)               │        │
│  │         Person, PersonName, NamedEntity          │        │
│  └─────────────────────────────────────────────────┘        │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Defining Module Boundaries

Use `package-info.java` to declare modules and their allowed dependencies:

```java
@ApplicationModule(
    displayName = "Owner Management",
    allowedDependencies = "model"
)
package org.springframework.samples.petclinic.owner;
```

```java
@ApplicationModule(
    displayName = "Vet Management",
    allowedDependencies = { "model", "owner::events" }  // Only event API, not internals
)
package org.springframework.samples.petclinic.vet;
```

**Key concept:** `owner::events` means the vet module can only access the `events` subpackage of owner, not its internal implementation.

### Exposing Event APIs

Create a subpackage for events that other modules can depend on:

```
owner/
├── package-info.java          # @ApplicationModule
├── Owner.java                 # Internal
├── OwnerRepository.java       # Internal
├── OwnerController.java       # Internal
└── events/
    ├── package-info.java      # Named interface
    └── PetAdoptedEvent.java   # Public API
```

### Verifying Module Structure

```java
class ModulithStructureTest {
    ApplicationModules modules = ApplicationModules.of("org.springframework.samples.petclinic");

    @Test
    void verifiesModularStructure() {
        modules.verify();  // Fails if boundaries are violated
    }

    @Test
    void generateDocumentation() {
        new Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml();
    }
}
```

---

## Architecture Enforcement with ArchUnit

Both Spring Modulith and jMolecules use [ArchUnit](https://www.archunit.org/) under the hood to enforce architectural rules at test time. This catches violations during CI/CD rather than at runtime.

### Spring Modulith + ArchUnit

Spring Modulith's `modules.verify()` internally uses ArchUnit to check:
- No cycles between modules
- Modules only access their declared dependencies
- Internal packages are not accessed from outside

You don't need to write ArchUnit rules manually—Spring Modulith generates them from your `@ApplicationModule` declarations.

### jMolecules Layered Architecture

jMolecules provides annotations to mark which architectural layer a class belongs to:

```
┌─────────────────────────────────────────────────────────┐
│                   @InterfaceLayer                        │
│              Controllers, REST endpoints                 │
├─────────────────────────────────────────────────────────┤
│                   @ApplicationLayer                      │
│           Application services, use cases                │
├─────────────────────────────────────────────────────────┤
│                     @DomainLayer                         │
│         Entities, Value Objects, Domain Services         │
├─────────────────────────────────────────────────────────┤
│                 @InfrastructureLayer                     │
│            Repositories, external services               │
└─────────────────────────────────────────────────────────┘

        Dependencies flow DOWN only (enforced by ArchUnit)
```

**Annotating classes:**

```java
// Controllers - Interface Layer
@InterfaceLayer
@Controller
public class OwnerController { }

// Application Services - Application Layer
@ApplicationLayer
@Service
public class PetApplicationService { }

// Domain Model - Domain Layer (via package-info.java)
@DomainLayer
package org.springframework.samples.petclinic.model;

// Repositories - Infrastructure Layer
@InfrastructureLayer
@Repository
public interface OwnerRepository extends JpaRepository<Owner, OwnerId> { }
```

**The rule:** Domain layer cannot depend on Application, Interface, or Infrastructure layers. This keeps your domain model pure and framework-agnostic.

### jMolecules DDD Rules

jMolecules also enforces DDD tactical patterns:

- **Entities must have identity** — Classes implementing `Entity` must have an `@Id` field
- **Aggregates must be referenced by ID** — Use `Association<T, ID>` not direct references
- **Repositories only for Aggregate Roots** — Can't create repositories for child entities
- **Value Objects must be immutable** — Classes implementing `ValueObject` shouldn't have setters

### Enforcing Rules with ArchUnit Tests

Create a test that runs all jMolecules rules:

```java
@AnalyzeClasses(packages = "org.springframework.samples.petclinic")
public class JMoleculesRulesUnitTest {

    @ArchTest
    ArchRule dddRules = JMoleculesDddRules.all();

    @ArchTest
    ArchRule layeredArchitecture = JMoleculesArchitectureRules.ensureLayering();
}
```

**What happens when rules are violated:**

```
java.lang.AssertionError: Architecture Violation [Priority: MEDIUM] -
Rule 'classes that implement Entity should have identity' was violated (1 times):
    Class Pet does not have an @Id annotated field
```

This fails your build, forcing you to fix the violation before merging.

### Available jMolecules Architecture Styles

jMolecules supports multiple architecture patterns:

| Style | Annotations | Use Case |
|-------|-------------|----------|
| **Layered** | `@DomainLayer`, `@ApplicationLayer`, `@InfrastructureLayer`, `@InterfaceLayer` | Traditional enterprise apps |
| **Hexagonal** | `@PrimaryPort`, `@SecondaryPort`, `@PrimaryAdapter`, `@SecondaryAdapter` | Ports & adapters |
| **Onion** | `@DomainModelRing`, `@DomainServiceRing`, `@ApplicationServiceRing`, `@InfrastructureRing` | Clean architecture |

This project uses the **layered architecture** style.

---

## Project Setup

### Dependencies (pom.xml)

```xml
<!-- Spring Modulith -->
<dependency>
    <groupId>org.springframework.modulith</groupId>
    <artifactId>spring-modulith-starter-core</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.modulith</groupId>
    <artifactId>spring-modulith-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- jMolecules DDD -->
<dependency>
    <groupId>org.jmolecules</groupId>
    <artifactId>jmolecules-ddd</artifactId>
</dependency>
<dependency>
    <groupId>org.jmolecules</groupId>
    <artifactId>jmolecules-layered-architecture</artifactId>
</dependency>
<dependency>
    <groupId>org.jmolecules.integrations</groupId>
    <artifactId>jmolecules-jpa</artifactId>
</dependency>

<!-- ByteBuddy for JPA annotation weaving -->
<dependency>
    <groupId>org.jmolecules.integrations</groupId>
    <artifactId>jmolecules-bytebuddy-nodep</artifactId>
    <scope>provided</scope>
</dependency>

<!-- ArchUnit for architecture verification -->
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.jmolecules.integrations</groupId>
    <artifactId>jmolecules-archunit</artifactId>
    <scope>test</scope>
</dependency>
```

### ByteBuddy Plugin (for automatic @Entity weaving)

```xml
<plugin>
    <groupId>net.bytebuddy</groupId>
    <artifactId>byte-buddy-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals><goal>transform-extended</goal></goals>
        </execution>
    </executions>
    <configuration>
        <classPathDiscovery>true</classPathDiscovery>
    </configuration>
</plugin>
```

This allows you to write clean domain classes without `@Entity`—ByteBuddy adds them at compile time based on jMolecules interfaces.

---

## Running the Application

```bash
git clone https://github.com/spring-projects/spring-petclinic.git
cd spring-petclinic
./mvnw spring-boot:run
```

Access at http://localhost:8080

### Running Tests

```bash
# All tests including modulith verification
./mvnw test

# Generate modulith documentation (target/modulith-docs/)
./mvnw test -Dtest=ModulithStructureTest#writeDocumentation
```

### Database Options

```bash
# MySQL
docker run -e MYSQL_USER=petclinic -e MYSQL_PASSWORD=petclinic \
           -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=petclinic \
           -p 3306:3306 mysql:9.2
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql

# PostgreSQL
docker run -e POSTGRES_USER=petclinic -e POSTGRES_PASSWORD=petclinic \
           -e POSTGRES_DB=petclinic -p 5432:5432 postgres:18.0
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

---

## The Path to Microservices

This architecture is designed as a stepping stone:

1. **Module boundaries** are already defined and verified
2. **Events** decouple modules—ready for async messaging (Kafka, RabbitMQ)
3. **Aggregate boundaries** map naturally to service boundaries
4. **Type-safe IDs** prevent accidental coupling through shared primitives

When you're ready to extract a service, the module is already isolated.

---

## Quick Reference

| Pattern | jMolecules Type | Purpose |
|---------|-----------------|---------|
| Aggregate Root | `AggregateRoot<T, ID>` | Entry point to aggregate, owns repository |
| Entity | `Entity<AggregateRoot, ID>` | Has identity, belongs to aggregate |
| Value Object | `ValueObject` | Immutable, equality by value |
| Identifier | `Identifier` | Type-safe ID wrapper |
| Association | `Association<T, ID>` | Cross-aggregate reference (ID only) |
| Domain Event | `DomainEvent` | Notification of state change |
| Repository | `Repository<T, ID>` | Aggregate persistence |

---

## References

### DDD & Architecture
- [Domain-Driven Design](https://www.domainlanguage.com/ddd/) — Eric Evans (2003)
- [Implementing DDD Building Blocks in Java](https://odrotbohm.de/2020/03/Implementing-DDD-Building-Blocks-in-Java/) — Oliver Drotbohm
- [Tactical DDD Workshop](https://github.com/odrotbohm/tactical-ddd-workshop)

### Spring Modulith
- [Spring Modulith Reference](https://docs.spring.io/spring-modulith/reference/)
- [Spring Modulith GitHub](https://github.com/spring-projects/spring-modulith)

### jMolecules
- [jMolecules GitHub](https://github.com/xmolecules/jmolecules)
- [jMolecules Integrations](https://github.com/xmolecules/jmolecules-integrations)

---

## License

Released under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
