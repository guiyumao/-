package edu.university.lab.module.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.university.lab.module.report.dto.ReportQuery;
import edu.university.lab.module.report.dto.ReportRow;

public interface ReportService {

    Page<ReportRow> summaryRows(ReportQuery query);

    byte[] exportCsv(ReportQuery query);
}
