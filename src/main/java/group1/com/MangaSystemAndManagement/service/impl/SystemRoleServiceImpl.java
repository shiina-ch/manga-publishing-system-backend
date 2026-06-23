package group1.com.MangaSystemAndManagement.service.impl;
import group1.com.MangaSystemAndManagement.dto.request.SystemRoleRequest;

import group1.com.MangaSystemAndManagement.model.SystemRole;
import group1.com.MangaSystemAndManagement.model.SystemRoleName;
import group1.com.MangaSystemAndManagement.repository.SystemRoleRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.SystemRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SystemRoleServiceImpl implements SystemRoleService {

    private final SystemRoleRepository systemRoleRepository;

    @Override
    public SystemRole create(SystemRoleRequest request) {
        throw immutableRoleDefinitions();
    }

    @Override
    public Optional<SystemRole> findById(Long id) {
        return systemRoleRepository.findById(id);
    }

    @Override
    public List<SystemRole> findAll() {
        return systemRoleRepository.findAll();
    }

    @Override
    public SystemRole update(Long id, SystemRoleRequest request) {
        throw immutableRoleDefinitions();
    }

    @Override
    public void delete(Long id) {
        throw immutableRoleDefinitions();
    }

    @Override
    public Optional<SystemRole> findByName(String name) {
        return systemRoleRepository.findAllByRoleNameIgnoreCase(SystemRoleName.from(name).name())
                .stream()
                .findFirst();
    }

    private UnsupportedOperationException immutableRoleDefinitions() {
        return new UnsupportedOperationException(
                "System role definitions are fixed and cannot be created, renamed, or deleted");
    }
}

