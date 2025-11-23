# Hexagonal Architecture Migration - Comparison

This document compares the **Pragmatic Hexagonal** approach (Phase 1) with the **Full Orthogonal** approach (Phase 2).

## Phase 1: Pragmatic Hexagonal Architecture ✅ COMPLETED

### Structure (Truly Pragmatic - Final Implementation)

```
owner/
├── domain/
│   └── ports/                          # Domain interfaces (@SecondaryPort)
│       ├── OwnerRepositoryPort.java
│       ├── PetTypeRepositoryPort.java
│       └── EventPublisherPort.java
│
├── PetApplicationService.java         # Application service (@ApplicationLayer)
├── PetController.java                 # Web controller
│
├── Owner.java                         # Domain models (with jMolecules ByteBuddy)
├── Pet.java
├── OwnerRepository.java               # Spring Data JPA + implements OwnerRepositoryPort
├── PetTypeRepository.java             # Spring Data JPA + implements PetTypeRepositoryPort
└── events/
    └── PetAdoptedEvent.java
```

**Key simplification:** Repositories directly extend port interfaces - no separate adapter classes needed!

### jMolecules Annotations Used

| Layer | Annotation | Purpose |
|-------|-----------|---------|
| **Domain Ports** | `@SecondaryPort` | Interfaces domain needs from infrastructure |
| **Application** | `@ApplicationLayer` | Application service implementation |
| **Infrastructure** | `@InfrastructureLayer` | Repository implementations |

**Note:** We use `@InfrastructureLayer` (from layered architecture) for repositories instead of `@SecondaryAdapter` since repositories directly extend port interfaces rather than being separate adapter classes.

### Key Characteristics

✅ **Domain models with ByteBuddy**
- Source code has NO `@Entity`, `@Table` annotations
- jMolecules ByteBuddy adds them at compile time
- Still some JPA concerns (`@Embedded`, `@JoinColumn`)

✅ **Port interfaces for dependency inversion**
- Domain defines `OwnerRepositoryPort` interface with domain-specific methods
- Spring Data repositories extend both `JpaRepository` AND the port interface
- No separate adapter classes needed

✅ **No mapping code needed**
- Domain models ARE JPA entities (via ByteBuddy)
- Repositories provide persistence without boilerplate mapping

✅ **Clean dependency flow**
```
Infrastructure → Application → Domain
(PrimaryAdapter)  (UseCase)     (Ports)
```

### Line Count

| Component | Files | Lines |
|-----------|-------|-------|
| **Domain Ports** | 3 interfaces | ~100 lines |
| **Repository Extensions** | Modified 2 existing files | ~10 lines added |
| **Total NEW code** | 3 files | **~110 lines** |

**Key benefit:** Only ~110 lines of new code to achieve hexagonal architecture with dependency inversion!

### Implementation Example

**Port Interface** (domain-specific methods only):
```java
@SecondaryPort
public interface OwnerRepositoryPort extends AssociationResolver<Owner, OwnerId> {
    Page<Owner> findByNameLastNameStartingWith(String lastName, Pageable pageable);

    // Note: Standard CRUD methods (findById, save, etc.) are inherited
    // from JpaRepository - don't redeclare them to avoid method ambiguity
}
```

**Repository Implementation** (extends both):
```java
@InfrastructureLayer
@Repository
public interface OwnerRepository
        extends JpaRepository<Owner, OwnerId>,
                AssociationResolver<Owner, OwnerId>,
                OwnerRepositoryPort {  // ← Also implements the port

    Page<Owner> findByNameLastNameStartingWith(String lastName, Pageable pageable);
}
```

**Application Service** uses concrete repository:
```java
@ApplicationLayer
@Service
public class PetApplicationService {
    private final OwnerRepository ownerRepository;          // ← Concrete repo (satisfies port)
    private final PetTypeRepository petTypeRepository;      // ← Concrete repo (satisfies port)
    private final ApplicationEventPublisher events;         // ← Spring component
}
```

### Pros

✅ **Port interfaces with dependency inversion** - Domain defines what it needs
✅ **Minimal overhead** - Only ~110 lines of new code for hexagonal structure
✅ **No adapter boilerplate** - Repositories directly extend ports
✅ **No mapping code** - jMolecules ByteBuddy handles JPA annotations
✅ **Testable** - Can mock concrete repositories (which also satisfy ports)
✅ **Clean architecture** - Dependency flow toward domain

### Cons

⚠️ **Domain still has some JPA coupling** - `@Embedded`, `@JoinColumn`, etc.
⚠️ **ByteBuddy "magic"** - JPA annotations not visible in source
⚠️ **Not pure hexagonal** - Application service uses concrete repositories, not port abstractions
⚠️ **Method ambiguity** - Must avoid redeclaring JpaRepository methods in ports

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

| Aspect | Truly Pragmatic Hexagonal | Full Orthogonal |
|--------|-------------------|-----------------|
| **Structure** | ✅ Port interfaces only | ✅ Full hexagonal packages |
| **Annotations** | ✅ @SecondaryPort | ✅ @PrimaryPort, @SecondaryPort, @Adapter |
| **Domain purity** | ⚠️ Some JPA annotations | ✅ Zero infrastructure |
| **Lines of code** | ~110 new lines | ~2,200 new lines (+1900%) |
| **Adapter classes** | ✅ None (repos extend ports) | ❌ Separate adapter classes |
| **Mapping code** | ✅ None (ByteBuddy) | ❌ ~300 lines |
| **Duplication** | ✅ None | ❌ High (domain + entity) |
| **Testability** | ✅ Good (mock concrete repos) | ✅ Excellent (pure domain) |
| **Maintainability** | ✅ Minimal overhead | ⚠️ High overhead |
| **Framework independence** | ⚠️ Locked to JPA | ✅ Can swap easily |
| **Performance** | ✅ No mapping overhead | ⚠️ Mapping overhead |
| **Suitable for** | ✅ Most applications | ⚠️ Complex domains, microservices |

---

## Recommendation

### For PetClinic (and most Spring applications):
**Use Truly Pragmatic Hexagonal (Phase 1) ✅ IMPLEMENTED**
- Achieves dependency inversion with minimal code (~110 lines)
- Repositories extend port interfaces - no adapter boilerplate
- Domain is quite pure (thanks to jMolecules ByteBuddy)
- Only ~5% overhead compared to traditional Spring Data approach
- Educational: clearly shows port/adapter concept

### When to use Full Orthogonal (Phase 2):
- **Complex business domains** with rich domain logic that must be framework-independent
- **Planning microservices migration** (domains will run in different processes)
- **Multiple persistence strategies** (need to swap JPA for MongoDB, DynamoDB, etc.)
- **Team with strong DDD culture** that values absolute domain purity
- **Large codebase** where ~1,400 lines of mapping code is negligible

---

## Implementation Status

### Phase 1: Truly Pragmatic Hexagonal ✅ COMPLETED

**What was implemented:**
1. ✅ Created domain port interfaces (`@SecondaryPort`)
   - `OwnerRepositoryPort` - defines owner persistence needs
   - `PetTypeRepositoryPort` - defines pet type repository needs
   - `EventPublisherPort` - defines event publishing needs

2. ✅ Extended Spring Data repositories to implement ports
   - `OwnerRepository extends JpaRepository, OwnerRepositoryPort`
   - `PetTypeRepository extends JpaRepository, PetTypeRepositoryPort`
   - No separate adapter classes needed!

3. ✅ Fixed method ambiguity
   - Port interfaces only declare domain-specific methods
   - Standard CRUD methods inherited from JpaRepository
   - Avoids "reference to save is ambiguous" compilation errors

4. ✅ Updated documentation
   - Comparison document explains pragmatic vs full orthogonal
   - Code examples show the implementation pattern

**Commits:**
- `d844911` - Truly pragmatic hexagonal: repositories extend ports directly
- `86e023f` - Exclude infrastructure package from WebMvcTest component scan
- `f3a9eca` - Simplify to pragmatic hexagonal architecture
- `5898c20` - Fix method ambiguity in OwnerRepositoryPort

### Phase 2: Full Orthogonal (Optional - Not Started)

If needed in the future, this would involve:
1. Create pure domain models (Pet, Owner, Visit) - zero infrastructure dependencies
2. Create separate JPA entities (PetEntity, OwnerEntity, VisitEntity)
3. Implement mapping code in adapter classes (~300 lines)
4. Create separate `@SecondaryAdapter` classes for each repository
5. Update all tests to work with pure domain models

**Trade-off:** +1,400 lines of code for complete framework independence
