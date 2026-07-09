import type { ToastType } from "@/types/toast";
import { cx } from "@/utils/cx";
import { AlertCircle, CheckCircle, X } from "@untitledui/icons";
import { Button } from "../buttons/Button";

interface ToastProps {
  type: ToastType;
  message: string;
  onClose: () => void;
}

export default function Toast({ type, message, onClose }: ToastProps) {
  const isSuccess = type === "success";

  return (
    <div
      className={cx(
        "pointer-events-auto flex w-fit min-w-80 items-center gap-3 rounded-xl border px-3 py-3.5",
        isSuccess
          ? "border-brand-200 bg-brand-primary text-brand-600 "
          : "border-red-100 bg-error-primary text-error-500"
      )}
      role={isSuccess ? "status" : "alert"}
      aria-live={isSuccess ? "polite" : "assertive"}
    >
      <div className="mt-0.5 flex h-7 w-7 items-center justify-center rounded-full">
        {isSuccess ? <CheckCircle className="size-6 text" /> : <AlertCircle className="size-6 " />}
      </div>

      <p className={cx("flex-1 text-md", isSuccess ? "" : "")}>{message}</p>

      <Button color="tertiary" className="inline-flex items-center justify-center size-6" onClick={onClose}>
        <X className="text-gray-400" />
      </Button>
    </div>
  );
}
