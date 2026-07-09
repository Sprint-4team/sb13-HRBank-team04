import { useEffect, useState } from "react";
import type { DateRange } from "react-aria-components";
import { FilterLines, SearchMd } from "@untitledui/icons";
import { HistoryTypeLabels } from "@/constants/HistoryTypeLabels";
import { useDebouncedValue } from "@/hooks/use-debounced-value";
import { useHistoryListStore } from "@/store/historyStore";
import type { HistoryType } from "@/types/enums";
import { formatDateRangeISO } from "@/utils/date";
import { Button } from "../common/buttons/Button";
import { DateRangePicker } from "../common/date-picker/DateRangePicker";
import { DropdownButton } from "../common/dropdown/DropdownButton";
import { Input } from "../common/input/Input";

const HistoryFilterSection = () => {
  const { setFilters, filters, totalElements } = useHistoryListStore();
  const [isFilterActive, setIsFilterActive] = useState(false);
  const [committedRange, setCommittedRange] = useState<DateRange | null>(null);
  const [tempRange, setTempRange] = useState<{
    start: string | undefined;
    end: string | undefined;
  } | null>(null);

  const [employeeNumberInput, setEmployeeNumberInput] = useState("");
  const debouncedEmployeeNumber = useDebouncedValue(employeeNumberInput);

  const [memoInput, setMemoInput] = useState("");
  const debouncedMemo = useDebouncedValue(memoInput);

  const [ipAddressInput, setIpAddressInput] = useState("");
  const debouncedIpAddress = useDebouncedValue(ipAddressInput);

  useEffect(() => {
    setFilters({ employeeNumber: debouncedEmployeeNumber });
  }, [debouncedEmployeeNumber, setFilters]);

  useEffect(() => {
    setFilters({ memo: debouncedMemo });
  }, [debouncedMemo, setFilters]);

  useEffect(() => {
    setFilters({ ipAddress: debouncedIpAddress });
  }, [debouncedIpAddress, setFilters]);

  const handleToggleFilter = () => {
    setIsFilterActive((prev) => !prev);
  };

  const handleRangeChange = (value: DateRange | null) => {
    const formattedDate = formatDateRangeISO(value);
    setTempRange(formattedDate);
    setCommittedRange(value);
  };

  const handleRangeApply = () => {
    setFilters({
      atFrom: tempRange?.start,
      atTo: tempRange?.end,
    });
  };

  const handleRangeCancel = () => {
    setTempRange(null);
    setCommittedRange(null);
    setFilters({
      atFrom: "",
      atTo: "",
    });
  };

  return (
    <div className="flex flex-col gap-4">
      <span className="text-tertiary text-sm">총 {totalElements}건</span>
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Input
            icon={SearchMd}
            iconClassName="w-5 h-5 stroke-black"
            placeholder="사번을 입력해주세요"
            className="w-80"
            onChange={(value) => setEmployeeNumberInput(value)}
          />
          <DropdownButton
            label={HistoryTypeLabels}
            value={filters.type}
            placeholder="유형"
            onChange={(value) => {
              setFilters({ type: value as HistoryType });
            }}
            className="h-10 w-[120px]"
          />
          <Button
            iconLeading={<FilterLines className="stroke-black" size={20} />}
            color="secondary"
            className="hover:bg-primary_hover h-8 w-8 data-icon-only:p-0"
            onClick={handleToggleFilter}
          />
        </div>
      </div>
      {isFilterActive && (
        <div className="flex items-center gap-3">
          <Input
            placeholder="내용을 입력해주세요"
            className="w-80"
            onChange={(value) => setMemoInput(value)}
          />
          <Input
            placeholder="IP 주소를 입력해주세요"
            className="w-80"
            onChange={(value) => setIpAddressInput(value)}
          />
          <DateRangePicker
            placeholder="날짜를 선택해주세요"
            value={committedRange}
            onChange={(value) => handleRangeChange(value)}
            onApply={handleRangeApply}
            onCancel={handleRangeCancel}
          />
        </div>
      )}
    </div>
  );
};

export default HistoryFilterSection;
