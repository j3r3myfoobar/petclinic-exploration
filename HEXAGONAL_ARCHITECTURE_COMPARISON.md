# Hexagonal Architecture Migration - Comparison

This document compares the **Pragmatic Hexagonal** approach (Phase 1) with the **Full Orthogonal** approach (Phase 2).

## Phase 1: Pragmatic Hexagonal Architecture ✅ COMPLETED

### Structure

```
owner/
├── domain/
│   └── ports/                          # Domain interfaces (@SecondaryPort)
│       ├── OwnerRepositoryPort.java
│       ├── PetTypeRepositoryPort.java
│       └── EventPublisherPort.java
│
├── application/                        # Use cases (@Application, @PrimaryPort)
│   ├── PetManagementUseCase.java      # Interface - what can be done
│   └── PetManagementService.java      # Implementation - how it's done
│
├── infrastructure/
│   ├── persistence/                    # JPA adapters (@SecondaryAdapter)
│   │   ├── JpaOwnerRepositoryAdapter.java
│   │   └── JpaPetTypeRepositoryAdapter.java
│   ├── web/                           # HTTP adapters (@PrimaryAdapter)
│   │   └── PetWebController.java
│   └── events/                        # Event adapters (@SecondaryAdapter)
│       └── SpringEventPublisherAdapter.java
│
├── Owner.java                         # Domain models (with jMolecules ByteBuddy)
├── Pet.java
├── OwnerRepository.java               # Spring Data JPA interface
└── events/
    └── PetAdoptedEvent.java
```

### jMolecules Annotations Used

| Layer | Annotation | Purpose |
|-------|-----------|---------|
| **Domain Ports** | `@SecondaryPort` | Interfaces domain needs from infrastructure |
| **Application** | `@Application` | Application service implementation |
| **Application** | `@PrimaryPort` | Use case interfaces exposed to external world |
| **Infrastructure** | `@SecondaryAdapter` | Implements domain ports (repos, event publisher) |
| **Infrastructure** | `@PrimaryAdapter` | Drives application (controllers) |

### Key Characteristics

✅ **Domain models with ByteBuddy**
- Source code has NO `@Entity`, `@Table` annotations
- jMolecules ByteBuddy adds them at compile time
- Still some JPA concerns (`@Embedded`, `@JoinColumn`)

✅ **Port interfaces separate domain from infrastructure**
- Domain defines `OwnerRepositoryPort` interface
- Infrastructure provides `JpaOwnerRepositoryAdapter` implementation

✅ **No mapping code needed**
- Domain models ARE JPA entities (via ByteBuddy)
- Adapters just delegate to Spring Data repositories

✅ **Clean dependency flow**
```
Infrastructure → Application → Domain
(PrimaryAdapter)  (UseCase)     (Ports)
```

### Line Count

| Component | Files | Lines |
|-----------|-------|-------|
| **Domain Ports** | 3 interfaces | ~150 lines |
| **Application Layer** | 2 files (interface + service) | ~230 lines |
| **Infrastructure Adapters** | 5 files | ~300 lines |
| **Package-info** | 5 files | ~100 lines |
| **Total NEW code** | 15 files | **~780 lines** |

### Dependencies

**Application Service** depends on:
```java
@Application
public class PetManagementService implements PetManagementUseCase {
    private final OwnerRepositoryPort ownerRepository;      // ← Domain port
    private final PetTypeRepositoryPort petTypeRepository;  // ← Domain port
    private final EventPublisherPort eventPublisher;        // ← Domain port
}
```

**Web Controller** depends on:
```java
@PrimaryAdapter
public class PetWebController {
    private final PetManagementUseCase petManagementUseCase;  // ← Use case interface
    private final OwnerRepositoryPort ownerRepository;        // ← Domain port (for @ModelAttribute)
}
```

### Pros

✅ **Hexagonal architecture structure** - Clear separation of concerns
✅ **Minimal code duplication** - jMolecules ByteBuddy eliminates mapping
✅ **Port interfaces** - Dependency inversion principle applied
✅ **Testable** - Can mock ports for testing
✅ **Low overhead** - ~780 lines of new code for full hexagonal structure

### Cons

⚠️ **Domain still has some JPA coupling** - `@Embedded`, `@JoinColumn`, etc.
⚠️ **ByteBuddy "magic"** - JPA annotations not visible in source
⚠️ **Not pure DDD** - Domain models optimized for ORM

---

## Phase 2: Full Orthogonal (Separate Domain/JPA Entities)

### What Would Change

#### Current (Pragmatic):
```java
// owner/Pet.java - Domain model (also JPA entity via ByteBuddy)
public class Pet implements Entity<Owner, PetId> {
    private PetId id;
    @Embedded  // ← Still some JPA
    private BirthDate birthDate;
    @JoinColumn(name = "pet_id")  // ← Still some JPA
    private Set<Visit> visits;

    public boolean isElderly() { ... }  // ← Domain logic
}
```

#### Full Orthogonal:
```java
// owner/domain/model/Pet.java - PURE domain (zero JPA)
public class Pet implements Entity<Owner, PetId> {
    private final PetId id;
    private BirthDate birthDate;
    private final List<Visit> visits;

    public boolean isElderly() { ... }  // ← Domain logic

    // No setters - immutable where possible
    public Pet withBirthDate(BirthDate date) {
        return new Pet(this.id, date, this.visits);
    }
}
```

```java
// owner/infrastructure/persistence/PetEntity.java - JPA entity
@Entity
@Table(name = "pets")
class PetEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "pet_id")
    @OrderBy("date ASC")
    private Set<VisitEntity> visits;

    // Mapping methods
    public Pet toDomain() { ... }
    public static PetEntity fromDomain(Pet pet) { ... }
}
```

### Additional Code Needed

| Component | Estimated Lines |
|-----------|----------------|
| **Pure domain models** (Owner, Pet, Visit) | ~400 lines |
| **JPA entities** (OwnerEntity, PetEntity, VisitEntity) | ~500 lines |
| **Mapping code** (domain ↔ JPA) | ~300 lines |
| **Updated adapters** (with mapping logic) | ~200 lines |
| **Total ADDITIONAL code** | **~1,400 lines** |

### Updated Adapter Example

```java
@SecondaryAdapter
@Repository
public class JpaOwnerRepositoryAdapter implements OwnerRepositoryPort {

    private final SpringDataOwnerRepository jpaRepo;

    @Override
    public Optional<Owner> findById(OwnerId id) {
        return jpaRepo.findById(id.value())
            .map(this::toDomain);  // ← NEW: Mapping required
    }

    @Override
    public Owner save(Owner owner) {
        OwnerEntity entity = toEntity(owner);  // ← NEW: Mapping required
        OwnerEntity saved = jpaRepo.save(entity);
        return toDomain(saved);
    }

    // NEW: Mapping methods
    private Owner toDomain(OwnerEntity entity) {
        return new Owner(
            new OwnerId(entity.getId()),
            new Name(entity.getFirstName(), entity.getLastName()),
            entity.getAddress() != null ?
                Address.of(entity.getAddress(), entity.getCity()) : null,
            entity.getPets().stream()
                .map(this::petToDomain)
                .collect(Collectors.toList())
        );
    }

    private OwnerEntity toEntity(Owner domain) {
        OwnerEntity entity = new OwnerEntity();
        entity.setId(domain.getId().value());
        entity.setFirstName(domain.getName().getFirstName());
        entity.setLastName(domain.getName().getLastName());
        // ... map all fields
        return entity;
    }
}
```

### Pros of Full Orthogonal

✅ **Complete domain purity** - Zero infrastructure dependencies
✅ **Domain is framework-agnostic** - Could swap JPA for anything
✅ **True DDD** - Domain optimized for business, not persistence
✅ **Immutability** - Domain can use immutable objects
✅ **Maximum testability** - Domain tests need zero infrastructure

### Cons of Full Orthogonal

❌ **~1,400 additional lines of code** (+180% compared to pragmatic)
❌ **Duplication** - Fields exist in both domain and entity classes
❌ **Mapping bugs** - Easy to forget to map a field
❌ **Maintenance overhead** - Two classes to update for each change
❌ **Complexity** - More moving parts
❌ **Performance** - Mapping adds overhead

---

## Comparison Table

| Aspect | Pragmatic Hexagonal | Full Orthogonal |
|--------|-------------------|-----------------|
| **Structure** | ✅ Hexagonal packages | ✅ Hexagonal packages |
| **Annotations** | ✅ @PrimaryPort, @SecondaryPort, etc. | ✅ @PrimaryPort, @SecondaryPort, etc. |
| **Domain purity** | ⚠️ Some JPA annotations | ✅ Zero infrastructure |
| **Lines of code** | ~780 new lines | ~2,200 new lines (+180%) |
| **Mapping code** | ✅ None (ByteBuddy) | ❌ ~300 lines |
| **Duplication** | ✅ None | ❌ High (domain + entity) |
| **Testability** | ✅ Good (can mock ports) | ✅ Excellent (pure domain) |
| **Maintainability** | ✅ Low overhead | ⚠️ Higher overhead |
| **Framework independence** | ⚠️ Locked to JPA | ✅ Can swap easily |
| **Performance** | ✅ No mapping overhead | ⚠️ Mapping overhead |
| **Suitable for** | ✅ Most applications | ⚠️ Complex domains, microservices migration |

---

## Recommendation

### For PetClinic (tutorial/learning project):
**Use Pragmatic Hexagonal (Phase 1)**
- Achieves 80% of hexagonal benefits
- Only 20% of the overhead
- Domain is already quite pure (thanks to jMolecules ByteBuddy)
- Simpler to maintain

### When to use Full Orthogonal (Phase 2):
- **Complex business domains** with rich domain logic
- **Planning microservices migration** (domains will run in different processes)
- **Multiple persistence strategies** (need to swap JPA for MongoDB, etc.)
- **Team with strong DDD culture** that values domain purity over pragmatism
- **Large codebase** where the ~1,400 line overhead is negligible

---

## Next Steps

### If staying with Pragmatic:
1. ✅ Commit Phase 1 changes
2. ✅ Update documentation
3. ✅ Run tests
4. ✅ Create ADR documenting the decision

### If proceeding to Full Orthogonal:
1. Create pure domain models (Pet, Owner, Visit)
2. Create JPA entities (PetEntity, OwnerEntity, VisitEntity)
3. Implement mapping in adapters
4. Update all tests
5. Compare and decide

**Which would you like to proceed with?**
