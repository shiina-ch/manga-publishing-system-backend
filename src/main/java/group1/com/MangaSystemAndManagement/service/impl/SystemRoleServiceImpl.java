package group1.com.MangaSystemAndManagement.service.impl;
import group1.com.MangaSystemAndManagement.dto.request.SystemRoleRequest;

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
    public SystemRole create(SystemRoleRequest request) {
        SystemRole entity = new SystemRole();
        org.springframework.beans.BeanUtils.copyProperties(request, entity);
        return systemRoleRepository.save(entity);
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
    public SystemRole update(Long id, SystemRoleRequest request) {
        SystemRole entity = systemRoleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("SystemRole not found with id " + id));
        org.springframework.beans.BeanUtils.copyProperties(request, entity);
        return systemRoleRepository.save(entity);
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

