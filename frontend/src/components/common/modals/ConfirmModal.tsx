import React from "react";
import { BaseModal } from "./BaseModal";
import { Button } from "../buttons/Button";
import { AlertCircle } from "@untitledui/icons";

interface ConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  children: React.ReactNode;
}

const ConfirmModal = ({ isOpen, onClose, onConfirm, children }: ConfirmModalProps) => {
  return (
    <BaseModal isOpen={isOpen} onOpenChange={onClose} className="w-80 h-52" contentClassName="pt-4 px-5 pb-6">
      <div className="flex flex-col items-center -mt-4">
        <AlertCircle data-icon className="text-fg-error-secondary mb-2" />
        <div className="text-center">{children}</div>
      </div>
      <div className="flex gap-3 justify-between">
        <Button color="secondary" className="w-full" onClick={onClose}>
          취소
        </Button>
        <Button type="submit" color="primary-destructive" className="w-full" onClick={onConfirm}>
          삭제하기
        </Button>
      </div>
    </BaseModal>
  );
};

export default ConfirmModal;
