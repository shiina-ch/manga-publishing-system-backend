package group1.com.MangaSystemAndManagement.service.impl;

import group1.com.MangaSystemAndManagement.model.SystemRole;
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
    @Transactional
    public SystemRole create(SystemRole systemRole) {
        return systemRoleRepository.save(systemRole);
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
    @Transactional
    public SystemRole update(Long id, SystemRole systemRole) {
        return systemRoleRepository.findById(id).map(existingRole -> {
            existingRole.setRoleName(systemRole.getRoleName());
            existingRole.setAccount(systemRole.getAccount());
            return systemRoleRepository.save(existingRole);
        }).orElseThrow(() -> new RuntimeException("SystemRole not found with id: " + id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!systemRoleRepository.existsById(id)) {
            throw new RuntimeException("SystemRole not found with id: " + id);
        }
        systemRoleRepository.deleteById(id);
    }

    @Override
    public Optional<SystemRole> findByName(String name) {
        return Optional.ofNullable(systemRoleRepository.findByRoleName(name));
    }
}

