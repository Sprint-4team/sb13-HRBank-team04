import { useEffect, useState } from "react";
import type { DateRange } from "react-aria-components";
import { FilterLines, Plus, SearchMd } from "@untitledui/icons";
import { EmploymentStateLabels } from "@/constants/EmploymentStateLabels";
import { useDebouncedValue } from "@/hooks/use-debounced-value";
import { useEmployeeListStore } from "@/store/employeeStore";
import type { EmployeeStatus } from "@/types/enums";
import { formatDateRange } from "@/utils/date";
import { Button } from "../common/buttons/Button";
import { DateRangePicker } from "../common/date-picker/DateRangePicker";
import { DropdownButton } from "../common/dropdown/DropdownButton";
import { Input } from "../common/input/Input";
import CreateUpdateEmployeeModal from "./CreateUpdateEmployeeModal";

const EmployeeFilterSection = () => {
  const { setFilters, filters, totalElements } = useEmployeeListStore();
  const [isFilterActive, setIsFilterActive] = useState(false);
  const [committedRange, setCommittedRange] = useState<DateRange | null>(null);
  const [tempRange, setTempRange] = useState<{
    start: string;
    end: string;
  } | null>(null);

  const [isCreateModalOpen, setIsCreateModalOpen] = useState<boolean>(false);

  const [nameOrEmailInput, setNameOrEmailInput] = useState("");
  const debouncedNameOrEmail = useDebouncedValue(nameOrEmailInput);

  const [employeeNumberInput, setEmployeeNumberInput] = useState("");
  const debouncedEmployeeNumber = useDebouncedValue(employeeNumberInput);

  const [departmentNameInput, setDepartmentNameInput] = useState("");
  const debouncedDepartmentName = useDebouncedValue(departmentNameInput);

  const [positionInput, setPositionInput] = useState("");
  const debouncedPosition = useDebouncedValue(positionInput);

  useEffect(() => {
    setFilters({ nameOrEmail: debouncedNameOrEmail });
  }, [debouncedNameOrEmail, setFilters]);

  useEffect(() => {
    setFilters({ employeeNumber: debouncedEmployeeNumber });
  }, [debouncedEmployeeNumber, setFilters]);

  useEffect(() => {
    setFilters({ departmentName: debouncedDepartmentName });
  }, [debouncedDepartmentName, setFilters]);

  useEffect(() => {
    setFilters({ position: debouncedPosition });
  }, [debouncedPosition, setFilters]);

  const handleToggleFilter = () => {
    setIsFilterActive((prev) => !prev);
  };

  const handleRangeChange = (value: DateRange | null) => {
    const formattedDate = formatDateRange(value);
    setTempRange(formattedDate);
    setCommittedRange(value);
  };

  const handleRangeApply = () => {
    setFilters({
      hireDateFrom: tempRange?.start,
      hireDateTo: tempRange?.end,
    });
  };

  const handleRangeCancel = () => {
    setTempRange(null);
    setCommittedRange(null);
    setFilters({
      hireDateFrom: undefined,
      hireDateTo: undefined,
    });
  };

  const handleClickCreateButton = () => {
    setIsCreateModalOpen(true);
  };

  return (
    <div className="flex flex-col gap-4">
      <span className="text-tertiary text-sm">총 {totalElements}명</span>
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Input
            icon={SearchMd}
            iconClassName="w-5 h-5 stroke-black"
            placeholder="이름 또는 이메일을 입력해주세요"
            className="w-80"
            onChange={(value) => setNameOrEmailInput(value)}
          />
          <DropdownButton
            label={EmploymentStateLabels}
            value={filters.status}
            placeholder="상태"
            onChange={(value) => {
              setFilters({ status: value as EmployeeStatus });
            }}
            className="h-10 min-w-[110px]"
          />
          <Button
            iconLeading={<FilterLines className="stroke-black" size={20} />}
            color="secondary"
            className="hover:bg-primary_hover h-8 w-8 data-icon-only:p-0"
            onClick={handleToggleFilter}
          />
        </div>
        <Button
          iconLeading={<Plus data-icon color="white" />}
          onClick={handleClickCreateButton}
        >
          직원 등록하기
        </Button>
      </div>
      {isFilterActive && (
        <div className="flex items-center gap-3">
          <Input
            placeholder="사번을 입력해주세요"
            className="w-80"
            onChange={(value) => setEmployeeNumberInput(value)}
          />
          <Input
            placeholder="부서명을 입력해주세요"
            className="w-48"
            onChange={(value) => setDepartmentNameInput(value)}
          />
          <Input
            placeholder="직함을 입력해주세요"
            className="w-48"
            onChange={(value) => setPositionInput(value)}
          />
          <DateRangePicker
            placeholder="입사일을 선택해주세요"
            value={committedRange}
            onChange={(value) => handleRangeChange(value)}
            onApply={handleRangeApply}
            onCancel={handleRangeCancel}
          />
        </div>
      )}
      <CreateUpdateEmployeeModal
        employee={null}
        isOpen={isCreateModalOpen}
        onOpenChange={setIsCreateModalOpen}
      />
    </div>
  );
};

export default EmployeeFilterSection;
