package io.upgradepilot.scan.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/scans")
public class ScanController {

    private final ScanService scanService;

    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ScanResponse create(@RequestParam("file") MultipartFile file,
                               @RequestParam(value = "projectName", required = false) String projectName) {
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        if (file.isEmpty() || !original.toLowerCase().endsWith(".zip")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload a .zip of your project");
        }
        String name = (projectName == null || projectName.isBlank())
                ? original.substring(0, original.length() - 4)
                : projectName.trim();
        return scanService.createScan(name, file);
    }

    @GetMapping("/{id}")
    public ScanResponse get(@PathVariable UUID id) {
        return scanService.getScan(id);
    }

    @GetMapping
    public List<ScanSummary> list() {
        return scanService.recentScans();
    }
}
