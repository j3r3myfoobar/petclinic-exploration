# Layered Validation Pattern

This document explains the layered validation approach implemented in the PetClinic application, following Domain-Driven Design (DDD) best practices.

## Overview

The application uses a **layered validation pattern** that separates concerns between different architectural layers:

- **Web Layer (DTOs)**: Framework-specific validation using Bean Validation (JSR-303)
- **Application Layer (Controllers)**: Business rule validation that requires context
- **Domain Layer (Entities)**: Invariant enforcement through constructors and methods

## Architecture

```
┌─────────────────────────────────────────┐
│         Web Layer (Controllers)         │
│  - PetFormData DTO with @NotBlank, etc. │
│  - Framework validation happens here     │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│      Application Layer (Business)       │
│  - Duplicate name checks                 │
│  - Context-dependent validation          │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│         Domain Layer (Entities)         │
│  - Pet, Owner (pure domain objects)     │
│  - No framework annotations              │
└─────────────────────────────────────────┘
```

## Implementation

### 1. PetFormData DTO (Web Layer)

Located at: `src/main/java/org/springframework/samples/petclinic/owner/PetFormData.java`

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
    // Conversion methods
    public Pet toDomainObject(PetType type) { ... }
    public void updateDomainObject(Pet pet, PetType type) { ... }
    public static PetFormData fromDomainObject(Pet pet, ...) { ... }
}
```

**Responsibilities:**
- Handle web form binding
- Validate input format and basic constraints
- Convert between web representation and domain objects

### 2. Controller Methods (Application Layer)

Located at: `src/main/java/org/springframework/samples/petclinic/owner/PetController.java`

```java
@PostMapping("/pets/new-dto")
public String processCreationFormDto(
        Owner owner,
        @Valid PetFormData formData,
        BindingResult result,
        RedirectAttributes redirectAttributes) {

    // Business rule validation
    if (formData.name() != null && owner.getPet(formData.name(), true) != null) {
        result.rejectValue("name", "duplicate",
            "A pet named '" + formData.name() + "' already exists for this owner");
    }

    if (result.hasErrors()) {
        return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
    }

    // Convert DTO → Domain
    PetType type = PetFormData.findTypeByName(formData.typeName(), ...);
    Pet pet = formData.toDomainObject(type);

    owner.addPet(pet);
    this.owners.save(owner);

    return "redirect:/owners/{ownerId}";
}
```

**Responsibilities:**
- Coordinate validation between layers
- Apply business rules that require context (e.g., duplicate checks)
- Orchestrate domain object creation and persistence

### 3. Domain Entities (Domain Layer)

Located at: `src/main/java/org/springframework/samples/petclinic/owner/Pet.java`

```java
@Table(name = "pets")
public class Pet extends NamedEntity implements Entity<Owner, PetId> {
    // No @NotBlank, @NotNull, or other framework validation annotations
    // Pure domain logic only
}
```

**Responsibilities:**
- Maintain domain invariants
- Encapsulate business logic
- Remain framework-agnostic

## Comparison: Old vs New

### Old Approach (Entity-based Validation)

```java
// Controller binds directly to Pet entity
@PostMapping("/pets/new")
public String processCreationForm(
        Owner owner,
        @Valid Pet pet,  // ❌ Framework validation mixed with domain
        BindingResult result,
        ...) {
    // ...
}

// Domain entity has framework annotations
public class Pet {
    @NotBlank  // ❌ Framework coupling in domain
    private String name;
}

// Custom PetValidator needed
public class PetValidator implements Validator {
    // ❌ Duplicate validation logic
}
```

**Problems:**
- Framework annotations pollute domain model
- Mixed concerns (validation vs business logic)
- Harder to test domain objects independently
- Validation tied to Spring MVC

### New Approach (DTO-based Validation)

```java
// Controller uses DTO
@PostMapping("/pets/new-dto")
public String processCreationFormDto(
        Owner owner,
        @Valid PetFormData formData,  // ✅ Framework validation in DTO
        BindingResult result,
        ...) {
    // Convert DTO → Domain
    Pet pet = formData.toDomainObject(type);
}

// Domain entity is clean
public class Pet {
    private String name;  // ✅ No framework annotations
}

// No custom validator needed
// ✅ Bean Validation handles it
```

**Benefits:**
- Clean domain model (no framework coupling)
- Clear separation of concerns
- Easy to test each layer independently
- Can add REST API with different DTOs without touching domain

## Testing

### Unit Tests (PetFormDataTests.java)

Tests Bean Validation on the DTO:

```java
@Test
void testBlankNameIsInvalid() {
    PetFormData formData = new PetFormData("", "cat", LocalDate.of(2020, 1, 1));
    Set<ConstraintViolation<PetFormData>> violations = validator.validate(formData);
    assertThat(violations).hasSize(1);
}
```

### Integration Tests (PetControllerDtoTests.java)

Tests the full web layer with MockMvc:

```java
@Test
void testProcessCreationFormDtoWithBlankName() throws Exception {
    mockMvc.perform(post("/owners/{ownerId}/pets/new-dto", ownerId)
        .param("name", "")
        .param("typeName", "hamster"))
        .andExpect(model().attributeHasFieldErrorCode("petForm", "name", "NotBlank"));
}
```

## Endpoints

### DTO-based Endpoints (New)

- `GET  /owners/{ownerId}/pets/new-dto` - Show creation form
- `POST /owners/{ownerId}/pets/new-dto` - Process creation
- `GET  /owners/{ownerId}/pets/{petId}/edit-dto` - Show edit form
- `POST /owners/{ownerId}/pets/{petId}/edit-dto` - Process update

### Entity-based Endpoints (Legacy)

- `GET  /owners/{ownerId}/pets/new` - Show creation form
- `POST /owners/{ownerId}/pets/new` - Process creation
- `GET  /owners/{ownerId}/pets/{petId}/edit` - Show edit form
- `POST /owners/{ownerId}/pets/{petId}/edit` - Process update

Both sets currently coexist. The DTO-based endpoints will eventually replace the entity-based ones.

## View Layer Implementation

### Adaptive Template Design

The `createOrUpdatePetForm.html` template supports both DTO-based and entity-based validation approaches through conditional logic:

```html
<!-- Support both DTO-based (petForm) and entity-based (pet) validation -->
<form th:object="${petForm != null ? petForm : pet}" class="form-horizontal" method="post">
  <input type="hidden" name="id" th:value="${pet?.id}" th:if="${!pet.isNew()}" />
  <!-- ... -->

  <!-- Use 'typeName' for DTO endpoints, 'type' for legacy endpoints -->
  <input th:replace="~{fragments/selectField :: select ('Type',
    ${petForm != null ? 'typeName' : 'type'}, ${types})}" />
</form>
```

**Key Features:**

1. **Automatic Form Binding**:
   - Binds to `petForm` when available (DTO endpoints: `/pets/new-dto`, `/pets/{petId}/edit-dto`)
   - Falls back to `pet` for legacy endpoints (entity-based: `/pets/new`, `/pets/{petId}/edit`)

2. **Field Name Adaptation**:
   - Uses `typeName` field for DTO validation (matches `PetFormData` record component)
   - Uses `type` field for entity-based validation (matches `Pet` entity property)

3. **Backward Compatibility**:
   - Both endpoint sets continue to work with the same template
   - No breaking changes to existing functionality
   - Allows gradual migration without disruption

**Benefits:**

- Single template serves both validation approaches
- No template duplication during migration
- Easy to test both approaches in parallel
- Clean migration path: once DTO endpoints become default, remove conditional logic

## Migration Status

### ✅ Completed

- [x] Week 1: Create DTOs and parallel controller endpoints
- [x] Week 2: Add comprehensive tests for DTO validation
- [x] Week 3: Update views to use `petForm` instead of `pet`

### 🚧 In Progress

- [ ] Week 4: Switch default endpoints to use DTOs
- [ ] Week 5: Remove legacy endpoints and PetValidator

## Best Practices

### When to Use DTOs

✅ **Use DTOs for:**
- Web form binding
- REST API request/response objects
- Framework-specific validation
- Data transfer between layers

❌ **Don't use DTOs for:**
- Domain logic
- Persistence (use entities)
- Business rule validation (use controllers/services)

### Validation Guidelines

**Web Layer (DTOs):**
- Format validation (@NotBlank, @Email, @Pattern)
- Type validation (@NotNull, @Min, @Max)
- Temporal validation (@Past, @Future, @PastOrPresent)

**Application Layer (Controllers):**
- Business rules requiring context (duplicate checks)
- Cross-field validation
- Authorization checks

**Domain Layer (Entities):**
- Invariant enforcement via constructors
- State transition validation
- Domain constraints

## References

- [Implementing DDD Building Blocks in Java](https://odrotbohm.de/2020/03/Implementing-DDD-Building-Blocks-in-Java/) - Oliver Drotbohm
- [Tactical DDD Workshop](https://github.com/odrotbohm/tactical-ddd-workshop)
- [Spring MVC Validation](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-validation.html)
- [Bean Validation 3.0 Specification](https://jakarta.ee/specifications/bean-validation/3.0/)
