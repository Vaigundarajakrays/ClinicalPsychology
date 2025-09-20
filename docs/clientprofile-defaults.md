# Default Values in Entity Fields with `@Column` and `@Builder.Default`

## Why we use `columnDefinition`

* When we add **new fields** to an existing table with existing rows, if those fields are marked as `NOT NULL` but no default value is provided, the migration will fail.
* Example: Adding `chatCount` or `isPaidForAiChat` without defaults → database will throw a **null constraint violation**, because old rows don’t have values for these new columns.
* To avoid this, we specify a **DB-level default value**:

```java
@Column(nullable = false, columnDefinition = "integer default 0")
private Integer chatCount = 0;

@Column(nullable = false, columnDefinition = "boolean default false")
private boolean isPaidForAiChat = false;
```

👉 This ensures that:

* For **existing rows**, the DB will automatically fill in `0` and `false` during schema migration.
* For **new rows inserted directly into the DB (pgAdmin, SQL scripts, etc.)**, if no value is provided, DB assigns the default.

---

## Why we use `@Builder.Default`

* We are using Lombok’s `@Builder` to create objects in the **Client Register API**.
* Problem: Lombok’s `@Builder` **ignores field initializers** like `= 0` or `= false` unless we explicitly tell it.
* That means if we write:

```java
private boolean isPaidForAiChat = false;
```

and then do:

```java
ClientProfile client = ClientProfile.builder()
        .name("John")
        .email("john@example.com")
        .build();
```

👉 `isPaidForAiChat` will be `null` (or throw error for primitive types) instead of `false`.

### Solution:

Use `@Builder.Default`:

```java
@Column(nullable = false, columnDefinition = "integer default 0")
@Builder.Default
private Integer chatCount = 0;

@Column(nullable = false, columnDefinition = "boolean default false")
@Builder.Default
private boolean isPaidForAiChat = false;
```

👉 Now Lombok will respect the default values when the builder is used.

---

## Final Effect

* **DB Safety**: Existing data migration works without null errors, new rows via SQL use DB defaults.
* **Java Safety**: New objects built via Lombok `@Builder` will correctly have `0` and `false`.

✅ Double protection against null issues in both DB and code.
