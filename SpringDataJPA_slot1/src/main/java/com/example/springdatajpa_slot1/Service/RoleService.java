package com.example.springdatajpa_slot1.Service;

import org.springframework.stereotype.Service;
import com.example.springdatajpa_slot1.model.Role;
import java.util.Set;

@Service
public interface RoleService {
    public Set<Role> findByBookId(Integer bookId);
    public Set<Role> findByBookIdAndName(String bookId, String name);
    public Set<Role> findByName(String name);
    public Role save(Role role);
}
