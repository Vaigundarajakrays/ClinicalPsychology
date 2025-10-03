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

## Why sometimes `columnDefinition` fails

You might face errors like:

```
Caused by: org.postgresql.util.PSQLException: ERROR: syntax error at or near "default"
```

Example:

```java
@Column(nullable = false, columnDefinition = "varchar(3) default 'CAD'")
private String currency;
```

This fails because Hibernate passes the DDL string directly to Postgres, and quoting rules can break depending on dialect and escaping.

### Fix

Instead of mixing it inside `columnDefinition`, use Hibernate’s `@ColumnDefault`:

```java
@Column(nullable = false, columnDefinition = "varchar(3)")
@ColumnDefault("'CAD'")
private String currency;
```

👉 This works because:

* `columnDefinition = "varchar(3)"` defines the type.
* `@ColumnDefault("'CAD'")` separately adds the default expression.

---

## Issue with second method

If you use the second method:

```java
@Column(nullable = false)
@ColumnDefault("'CAD'")
private String currency;
```

Hibernate might still generate DDL like:

```
Hibernate: alter table if exists mentor_new add column currency varchar(3) default 'CAD' not null
```

even if the column already exists. This is because Hibernate tries to ensure DB schema matches the entity.

✅ Recommendation: For safety and to avoid repeated `alter table` executions, use:

```java
@Column(nullable = false)
@ColumnDefault("'CAD'")
private String currency;
```

This avoids specifying `columnDefinition` while still keeping a DB-level default.

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
* **Java Safety**: New objects built via Lombok `@Builder` will correctly have `0`, `false`, or `'CAD'`.

✅ Double protection against null issues in both DB and code.
