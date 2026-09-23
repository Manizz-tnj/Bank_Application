package com.bank.repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Abstract Generic Repository providing in-memory collection cache
 * backed by Java Object Serialization persistence in data/.
 * Demonstrates Generics, File Handling, Java I/O, Serialization, and Collections.
 *
 * @param <T>  Serializable entity type
 * @param <ID> Unique identifier type
 */
public abstract class AbstractFileRepository<T extends Serializable, ID> implements Repository<T, ID> {

    // Primary in-memory collection: HashMap / LinkedHashMap for predictable ordering
    protected final Map<ID, T> storage = new LinkedHashMap<>();
    private final String filePath;
    private final Object lock = new Object();

    /**
     * Initializes repository and loads persisted entities from the specified file path.
     * @param filePath Relative or absolute path to the .dat persistence file.
     */
    protected AbstractFileRepository(String filePath) {
        this.filePath = filePath;
        ensureDataDirectoryExists();
        loadFromFile();
    }

    /**
     * Abstract method requiring concrete subclasses to define how the primary key ID is extracted.
     */
    protected abstract ID extractId(T entity);

    private void ensureDataDirectoryExists() {
        try {
            Path path = Paths.get(filePath);
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not create data directory for " + filePath + ": " + e.getMessage());
        }
    }

    @Override
    public T save(T entity) {
        Objects.requireNonNull(entity, "Entity to save must not be null");
        ID id = extractId(entity);
        Objects.requireNonNull(id, "Extracted entity ID must not be null");

        synchronized (lock) {
            storage.put(id, entity);
            saveToFile();
        }
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        if (id == null) return Optional.empty();
        synchronized (lock) {
            return Optional.ofNullable(storage.get(id));
        }
    }

    @Override
    public List<T> findAll() {
        synchronized (lock) {
            return new ArrayList<>(storage.values());
        }
    }

    @Override
    public boolean deleteById(ID id) {
        if (id == null) return false;
        synchronized (lock) {
            if (storage.containsKey(id)) {
                storage.remove(id);
                saveToFile();
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean existsById(ID id) {
        if (id == null) return false;
        synchronized (lock) {
            return storage.containsKey(id);
        }
    }

    @Override
    public long count() {
        synchronized (lock) {
            return storage.size();
        }
    }

    @Override
    public void flush() {
        synchronized (lock) {
            saveToFile();
        }
    }

    /**
     * Serializes in-memory collection to the target .dat file.
     */
    protected void saveToFile() {
        File file = new File(filePath);
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            // Persist as an ArrayList
            List<T> snapshot = new ArrayList<>(storage.values());
            oos.writeObject(snapshot);
            oos.flush();
        } catch (IOException e) {
            System.err.println("Persistence Error: Failed to write to " + filePath + ": " + e.getMessage());
        }
    }

    /**
     * Reads serialized objects from disk and populates the in-memory cache.
     * Gracefully handles missing, empty, or corrupted data files.
     */
    @SuppressWarnings("unchecked")
    protected void loadFromFile() {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            return; // Fresh start or empty file, graceful fallback
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?> list) {
                storage.clear();
                for (Object item : list) {
                    try {
                        T entity = (T) item;
                        storage.put(extractId(entity), entity);
                    } catch (ClassCastException cce) {
                        System.err.println("Warning: Skipped incompatible item in " + filePath);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // File not yet created, safe to ignore
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Notice: Could not load data from " + filePath + " (" + e.getMessage() + "). Initializing with empty state.");
        }
    }
}
