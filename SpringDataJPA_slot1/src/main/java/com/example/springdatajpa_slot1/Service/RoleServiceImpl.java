package com.example.springdatajpa_slot1.Service;

import com.example.springdatajpa_slot1.model.Role;
import com.example.springdatajpa_slot1.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RoleServiceImpl implements RoleService {
    private RoleRepository roleRepository;
    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    @Override
    public Set<Role> findByBookId(Integer bookId) {
        return Set.of();
    }

    @Override
    public Set<Role> findByBookIdAndName(String bookId, String name) {
        return Set.of();
    }

    @Override
    public Set<Role> findByName(String name) {
        return Set.of();
    }

    @Override
    public Role save(Role role) {
        return roleRepository.save(role);
    }
}
